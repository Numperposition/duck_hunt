import java.util.ArrayList;

/**
 * @author Sherley Huang
 * @date 2019/9/9 11:51
 */
public class HMM {

    private double[][] A;
    private double[][] B;
    private double[] pi;
    private Integer[] observation = new Integer[100];
    private static final int N = 5; // number of states
    private static final int M = 9; // number of observation types
    private int T = 0; // length of the observation sequence

    public HMM() {
        this.init();
    }

    // 1. Initialization
    private void init() {
        A = new double[][] {{0.2, 0.05, 0.05, 0.05, 0.05},
                {0.075, 0.7, 0.2, 0.075, 0.075},
                {0.075, 0.075, 0.7, 0.2, 0.075},
                {0.075, 0.2, 0.075, 0.7, 0.075},
                {0.075, 0.075, 0.075, 0.075, 0.2}};
        B = new double[][] {{0.125, 0.125, 0.125, 0.125, 0.0, 0.125, 0.125, 0.125, 0.125},
                {0.36, 0.04, 0.36, 0.04, 0.04, 0.04, 0.04, 0.04, 0.04},
                {0.016, 0.016, 0.016, 0.225, 0.02, 0.225, 0.016, 0.45, 0.016},
                {0.15, 0.15, 0.15, 0.04, 0.02, 0.04, 0.15, 0.15, 0.15},
                {0.1125, 0.1125, 0.1125, 0.1125, 0.1, 0.1125, 0.1125, 0.1125, 0.1125}};
        pi = new double[] {0.2042, 0.19453, 0.2, 0.20345, 0.19782};

        for (int i = 0; i < observation.length; i++) {
            observation[i] = 0;
        }
    }

    public void modelTrain(Bird bird) {
        T = 0;
        for (int i = 0; i < bird.getSeqLength(); i++) {
            observation[i] = bird.getObservation(i);
            if (observation[i] != -1) {
                T++;
            } else {
                break;
            }
        }

        int maxIters = 100;
        int iters = 0;
        Double oldLogProb = -1e5;
        Double logProb = -1e4;

        while (iters < maxIters && (logProb - oldLogProb) > 0.00008312) {
            iters++;
            oldLogProb = logProb;
            logProb = BaumWelch(observation);
        }
    }

    private double BaumWelch(Integer[] emissionsArr) {
        /// 2. The α-pass
        double[][] alpha = new double[T][N];

        // compute α0(i)
        double[] c = new double[T];
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
        double[][] beta = new double[T][N];

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
        double[][] gamma = new double[T][N];
        double[][][] di_gamma = new double[T][N][N];

        for (int t = 0; t < T - 1; t++) {
            double denom = 0.0;
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
        double denom = 0.0;
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
                double numer = 0.0;
                denom = 0.0;
                for (int t = 0; t < T - 1; t++) {
                    numer += di_gamma[t][i][j];
                    denom += gamma[t][i];
                }
                this.A[i][j] = numer / denom;
            }
        }

        // re-estimate B
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                double numer = 0.0;
                denom = 0.0;
                for (int t = 0; t < T; t++) {
                    if (emissionsArr[t] == j) {
                        numer += gamma[t][i];
                    }
                    denom += gamma[t][i];
                }
                this.B[i][j] = numer / denom;
            }
        }

        /// 6. Compute log[P (O | λ)]
        double logProb = 0.0;
        for (int i = 0; i < T; i++) {
            logProb += Math.log(c[i]);
        }
        logProb = 0.0 - logProb;

        return logProb;
    }

    public double getProb(Bird bird) {
        T = bird.getSeqLength();
        for (int i = 0; i < T; i++) {
            observation[i] = bird.getObservation(i);
        }

        double[][] initPossibility = new double[1][N];
        for (int i = 0; i < A.length; i++) {
            initPossibility[0][i] = observation[i] * pi[i];
        }

        double[][] finalPossibility = initPossibility;
        for (int i = 0; i < T - 1; i++) {
            finalPossibility = forwardOneStep(finalPossibility, observation[i + 1]);
        }

        double result = 0.0;
        for (int i = 0; i < finalPossibility.length; i++) {
            result += finalPossibility[0][i];
        }

        return result;
    }

    /**
     * return the possibility of next movement MOVEMENT given observation OBSERVATION
     *
     * @param bird     Bird
     * @param movement int
     * @return Double
     */
    public double getProb(Bird bird, int movement) {
        T = bird.getSeqLength();
        observation[T] = movement;
        T++;

        double[][] initPossibility = new double[1][N];
        for (int i = 0; i < A.length; i++) {
            initPossibility[0][i] = observation[i] * pi[i];
        }

        double[][] finalPossibility = initPossibility;
        for (int i = 0; i < T - 1; i++) {
            finalPossibility = forwardOneStep(finalPossibility, observation[i + 1]);
        }

        double result = 0.0;
        for (int i = 0; i < finalPossibility.length; i++) {
            result += finalPossibility[0][i];
        }

        return result;
    }

    private double[][] forwardOneStep(double[][] initPossibility, int emission) {
        double[][] nextInitPossibility = new double[1][A.length];

        double[][] predictedA = matrixMultiply(initPossibility, A);
        double[][] emissionMatrix = new double[1][A.length];
        for (int i = 0; i < A.length; i++) {
            emissionMatrix[0][i] = B[i][emission];
        }

        for (int i = 0; i < A.length; i++) {
            nextInitPossibility[0][i] = emissionMatrix[0][i] * predictedA[0][i];
        }

        return nextInitPossibility;
    }

    private double[][] matrixMultiply(double[][] x, double[][] y) {
        int result_row = x.length;
        int result_line = y[0].length;
        double[][] result = new double[result_row][result_line];

        for (int i = 0; i < result_row; i++) {
            for (int j = 0; j < result_line; j++) {
                result[i][j] = 0.0;
            }
        }

        for (int i = 0; i < result_row; i++) {
            for (int j = 0; j < result_line; j++) {
                for (int k = 0; k < y.length; k++) {
                    result[i][j] += x[i][k] * y[k][j];
                }
            }
        }
        return result;
    }

}
