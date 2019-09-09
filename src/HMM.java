import java.util.ArrayList;

/**
 * @author Sherley Huang
 * @date 2019/9/9 11:51
 */
public class HMM {

    private Double[][] A;
    private Double[][] B;
    private Double[] pi;
    private ArrayList<Integer> emissionsArr;
    private static final int patternAmount = 5;
    private static final int moveAmount = Constants.COUNT_MOVE;

    public HMM(ArrayList<Integer> emissionsArr) {
        this.emissionsArr = emissionsArr;
    }

    private Double[] randomInit(int length) {
        Double[] result = new Double[length];
        Double sum = 0.0;

        for (int i = 0; i < length; i++) {
            result[i] = Math.random() / length * 2;
            sum += result[i];
        }

        for (int i = 0; i < length; i++) {
            result[i] = result[i] / sum; // ensure all result[i] adds up to 1
        }

        return result;
    }

    // 1. Initialization
    private void init() {
        A = new Double[patternAmount][patternAmount];
        B = new Double[patternAmount][moveAmount];
        pi = new Double[patternAmount];

        Double[] randomPi = this.randomInit(patternAmount);
        for (int i = 0; i < patternAmount; i++) {
            this.pi[i] = randomPi[i];

            Double[] randomA = this.randomInit(patternAmount);
            for (int j = 0; j < patternAmount; j++) {
                this.A[i][j] = randomA[j];
            }

            Double[] randomB = this.randomInit(moveAmount);
            for (int j = 0; j < moveAmount; j++) {
                this.B[i][j] = randomB[j];
            }
        }
    }

    public void modelTrain() {
        this.init();

        int maxIters = 50;
        int iters = 0;
        Double oldLogProb = Double.NEGATIVE_INFINITY;

        Double logProb = BaumWelch(emissionsArr);
        while (iters < maxIters && logProb > oldLogProb) {
            iters++;
            oldLogProb = logProb;
            logProb = BaumWelch(emissionsArr);
        }

//        StringBuilder stringBuilder = new StringBuilder();
//        stringBuilder.append(A.length).append(" ").append(A[0].length).append(" ");
//        for (int i = 0; i < A.length; i++) {
//            for (int j = 0; j < A[0].length; j++) {
//                stringBuilder.append(A[i][j]).append(" ");
//            }
//        }
//        String strA = stringBuilder.toString().trim();
//
//        stringBuilder = new StringBuilder();
//        stringBuilder.append(B.length).append(" ").append(B[0].length).append(" ");
//        for (int i = 0; i < B.length; i++) {
//            for (int j = 0; j < B[0].length; j++) {
//                stringBuilder.append(B[i][j]).append(" ");
//            }
//        }
//        String strB = stringBuilder.toString().trim();
//
//        System.out.println(strA);
//        System.out.println(strB);
    }

    private Double BaumWelch(ArrayList<Integer> emissionsArr) {
        int N = this.A.length;
        int M = this.B[0].length;
        int T = emissionsArr.size();

        /// 2. The α-pass
        Double[][] alpha = new Double[T][N];

        // compute α0(i)
        Double[] c = new Double[T];
        c[0] = 0.0;
        for (int i = 0; i < N; i++) {
            alpha[0][i] = this.pi[i] * this.B[i][emissionsArr.get(0)];
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
                alpha[t][i] *= this.B[i][emissionsArr.get(t)];
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
                    beta[t][i] += this.A[i][j] * this.B[j][emissionsArr.get(t + 1)]
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
                    denom += alpha[t][i] * this.A[i][j] * this.B[j][emissionsArr.get(t + 1)]
                            * beta[t + 1][j];
                }
            }
            for (int i = 0; i < N; i++) {
                gamma[t][i] = 0.0;
                for (int j = 0; j < N; j++) {
                    di_gamma[t][i][j] = (alpha[t][i] * this.A[i][j] *
                            this.B[j][emissionsArr.get(t + 1)] * beta[t + 1][j]) / denom;
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
            }
        }

        // re-estimate B
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                Double numer = 0.0;
                denom = 0.0;
                for (int t = 0; t < T; t++) {
                    if (emissionsArr.get(t) == j) {
                        numer += gamma[t][i];
                    }
                    denom += gamma[t][i];
                }
                this.B[i][j] = numer / denom;
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

    public Double[][] getA() {
        return A;
    }

    public Double[][] getB() {
        return B;
    }

    public Double[] getPi() {
        return pi;
    }


    public int getMostLikelyObservation(ArrayList<Integer> observations) {
        int firstEmission = observations.get(0);
        Double[][] initPossibility = new Double[1][patternAmount];
        for (int i = 0; i < A.length; i++) {
            initPossibility[0][i] = B[i][firstEmission] * pi[i];
        }

        Double[][] finalPossibility = initPossibility;
        for (int i = 0; i < observations.size() - 1; i++) {
            finalPossibility = this.forwardOneStep(finalPossibility, observations.get(i + 1));
        }

        Double maxPossibility = 0.0;
        int mostLikelyObservation = 0;
        for (int i = 0; i < Constants.COUNT_MOVE; i++) {
            finalPossibility = this.forwardOneStep(finalPossibility, i);
            Double possibility = 0.0;
            for (int j = 0; j < patternAmount; j++) {
                possibility += finalPossibility[0][j];
            }
            if (possibility > maxPossibility) {
                maxPossibility = possibility;
                mostLikelyObservation = i;
            }
        }

        return mostLikelyObservation;
    }

    private Double[][] forwardOneStep(Double[][] initPossibility, int emission) {
        Double[][] nextInitPossibility = new Double[1][patternAmount];

        Double[][] predictedA = this.matrixMultiply(initPossibility, A);
        Double[][] emissionMatrix = new Double[1][patternAmount];
        for (int i = 0; i < patternAmount; i++) {
            emissionMatrix[0][i] = B[i][emission];
        }

        for (int i = 0; i < patternAmount; i++) {
            nextInitPossibility[0][i] = emissionMatrix[0][i] * predictedA[0][i];
        }

        return nextInitPossibility;
    }

    private Double[][] matrixMultiply(Double[][] x, Double[][] y) {
        int result_row = x.length;
        int result_line = y[0].length;
        Double[][] result = new Double[result_row][result_line];

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
