import java.util.ArrayList;

/**
 * @author Sherley Huang
 * @date 2019/9/9 11:51
 */
public class HMM {

    private Double[][] A;
    private Double[][] B;
    private Double[] pi;
    private Integer[] observation = new Integer[100];
    private static final int N = 5; // number of states
    private static final int M = 9; // number of observation types
    private static final Double threshold = 4e-16;
    private int T = 0; // length of the observation sequence
    private int species = Constants.SPECIES_UNKNOWN;

    public HMM() {
        this.init();
    }

    // 1. Initialization
    private void init() {
        A = new Double[N][N];
        B = new Double[N][M];
        pi = new Double[N];

        Double INIT_A = 0.28; // initialization key value of A
        Double INIT_B = 0.0703; // initialization key value of B

        for (int i = 0; i < N; i++) {
            pi[i] = 1.0 / N;
            for (int j = 0; j < N; j++) {
                if (j == i) {
                    A[i][j] = 1.0 - (N - 1) * INIT_A;
                    ;
                } else {
                    A[i][j] = INIT_A;
                }
            }
            for (int j = 0; j < M; j++) {
                if (j == i) {
                    B[i][j] = 1.0 - (M - 1) * INIT_B;
                } else {
                    B[i][j] = INIT_B;
                }
            }
        }

        pi[0] = 0.40547;
        pi[1] = 0.29453;
        pi[2] = 0.3;

        B[0][2] += 0.0002;
        B[0][4] -= 0.0002;
        B[1][5] += 0.0001;
        B[1][6] -= 0.0001;
        B[2][5] += 0.00025;
        B[2][6] -= 0.00025;

        for (int i = 0; i < observation.length; i++) {
            observation[i] = 0;
        }
    }

    public void modelTrain(Bird bird, int species) {
        T = 0;
        for (int i = 0; i < bird.getSeqLength(); i++) {
            observation[i] = bird.getObservation(i);
            if (observation[i] != -1) {
                T++;
            } else {
                break;
            }
        }

        int maxIters = 200;
        int iters = 0;
        Double oldLogProb = -1e5;
        Double logProb = -1e4;

        while (iters < maxIters && (logProb - oldLogProb) > 0.00008312) {
            iters++;
            oldLogProb = logProb;
            logProb = BaumWelch(observation);
        }

        this.species = species;
//        return this;
    }

    private Double BaumWelch(Integer[] emissionsArr) {
        /// 2. The α-pass
        Double[][] alpha = new Double[T][N];

        // compute α0(i)
        Double[] c = new Double[T];
        c[0] = 0.0;
        for (int i = 0; i < N; i++) {
            alpha[0][i] = this.pi[i] * this.B[i][emissionsArr[0]];
            c[0] += alpha[0][i];
        }

        // scale the α0(i)
        c[0] = 1 / c[0];
        for (int i = 0; i < N; i++) {
            alpha[0][i] *= c[0];
        }

        // compute αt(i)
        for (int t = 1; t < T; t++) {
            c[t] = 0.0;
            for (int i = 0; i < N; i++) {
                alpha[t][i] = 0.0;
                for (int j = 0; j < N; j++) {
                    alpha[t][i] += alpha[t - 1][j] * this.A[j][i];
                }
                alpha[t][i] *= this.B[i][emissionsArr[0]];
                c[t] += alpha[t][i];
            }
            // scale αt(i)
            c[t] = 1 / c[t];
            for (int i = 0; i < N; i++) {
                alpha[t][i] *= c[t];
            }
        }

        /// 3. The β-pass
        Double[][] beta = new Double[T][N];

        // Let βT−1(i) = 1, scaled by cT−1
        for (int i = 0; i < N; i++) {
            beta[T - 1][i] = 1.0 * c[T - 1];
        }

        // β-pass
        for (int t = T - 2; t >= 0; t--) {
            for (int i = 0; i < N; i++) {
                beta[t][i] = 0.0;
                for (int j = 0; j < N; j++) {
                    beta[t][i] += this.A[i][j] * this.B[j][emissionsArr[t + 1]]
                            * beta[t + 1][j];
                }
                beta[t][i] *= c[t];
            }
        }

        /// 4. Compute γt(i, j) and γt(i)
        Double[][] gamma = new Double[T][N];
        Double[][][] di_gamma = new Double[T][N][N];

        for (int t = 0; t < T - 1; t++) {
            Double denom = 0.0;
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    denom += alpha[t][i] * this.A[i][j] * this.B[j][emissionsArr[t + 1]]
                            * beta[t + 1][j];
                }
            }
            for (int i = 0; i < N; i++) {
                gamma[t][i] = 0.0;
                for (int j = 0; j < N; j++) {
                    di_gamma[t][i][j] = (alpha[t][i] * this.A[i][j] *
                            this.B[j][emissionsArr[t + 1]] * beta[t + 1][j]) / denom;
                    gamma[t][i] += di_gamma[t][i][j];
                }
            }
        }
        // Special case for γT−1(i)
        Double denom = 0.0;
        for (int i = 0; i < N; i++) {
            denom += alpha[T - 1][i];
        }
        for (int i = 0; i < N; i++) {
            gamma[T - 1][i] = alpha[T - 1][i] / denom;
        }

        /// 5. Re-estimate A, B and π
        // re-estimate π
        for (int i = 0; i < N; i++) {
            this.pi[i] = gamma[0][i];
        }

        // re-estimate A
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                Double numer = 0.0;
                denom = 0.0;
                for (int t = 0; t < T - 1; t++) {
                    numer += di_gamma[t][i][j];
                    denom += gamma[t][i];
                }
                this.A[i][j] = numer / denom;
                if (A[i][j] < threshold) {
                    A[i][j] = threshold;
                }
            }
        }

        // re-estimate B
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                Double numer = 0.0;
                denom = 0.0;
                for (int t = 0; t < T; t++) {
                    if (emissionsArr[t] == j) {
                        numer += gamma[t][i];
                    }
                    denom += gamma[t][i];
                }
                this.B[i][j] = numer / denom;
                if (B[i][j] < threshold) {
                    B[i][j] = threshold;
                }
            }
        }

        /// 6. Compute log[P (O | λ)]
        Double logProb = 0.0;
        for (int i = 0; i < T; i++) {
            logProb += Math.log(c[i]);
        }
        logProb = 0.0 - logProb;

        return logProb;
    }

    /**
     * return the possibility of next movement MOVEMENT given observation OBSERVATION
     *
     * @param bird     Bird
     * @param movement int
     * @return Double
     */
    public Double getProb(Bird bird, int movement) {
        T = bird.getSeqLength();
        for (int i = 0; i < T; i++) {
            observation[i] = bird.getObservation(i);
        }
        if (movement != -1) {
            observation[T] = movement;
            T++;
        }

        /// 2. The α-pass
        Double[][] alpha = new Double[T][N];

        // compute α0(i)
        Double[] c = new Double[T];
        c[0] = 0.0;
        for (int i = 0; i < N; i++) {
            alpha[0][i] = this.pi[i] * this.B[i][observation[0]];
            c[0] += alpha[0][i];
        }

        // scale the α0(i)
        c[0] = 1 / c[0];
        for (int i = 0; i < N; i++) {
            alpha[0][i] *= c[0];
        }

        // compute αt(i)
        for (int t = 1; t < T; t++) {
            c[t] = 0.0;
            for (int i = 0; i < N; i++) {
                alpha[t][i] = 0.0;
                for (int j = 0; j < N; j++) {
                    alpha[t][i] += alpha[t - 1][j] * this.A[j][i];
                }
                alpha[t][i] *= this.B[i][observation[0]];
                c[t] += alpha[t][i];
            }
            // scale αt(i)
            c[t] = 1 / c[t];
            for (int i = 0; i < N; i++) {
                alpha[t][i] *= c[t];
            }
        }

        Double logProb = 0.0;
        for (int i = 0; i < T; i++) {
            logProb += Math.log(c[i]);
        }

        return logProb;
    }

    public int getSpecies() {
        return species;
    }

}
