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

    private void init2() {
        pi = new double[N];
        A = new double[N][N];
        B = new double[N][M];

        for(int i = 0; i < N-1; i++) {
            pi[i] = 1.0/N+(Math.random()-0.5)/100;
        }



        for (int i = 0; i < N; ++i) {
            for (int j = 0; j < N/2; ++j) {
                this.A[i][j] = 1.0/N + (Math.random()-0.5)/100;
            }
            for(int j = N/2; j < N; j++)
                this.A[i][j] = Math.abs(1.0/N - (Math.random()-0.5)/100);
        }
        for (int i = 0; i < N; ++i) {
            for (int j = 0; j < M/2; ++j) {
                this.B[i][j] = 1.0/M + (Math.random()-0.5)/100;
            }
            for(int j = M/2; j < M; j++)
                this.B[i][j] = Math.abs(1.0/M - (Math.random()-0.5)/100);
        }

    }

    // 1. Initialization
    private void init() {
//        A = new double[][]{{0.2, 0.05, 0.05, 0.05, 0.05},
//                {0.075, 0.7, 0.2, 0.075, 0.075},
//                {0.075, 0.075, 0.7, 0.2, 0.075},
//                {0.075, 0.2, 0.075, 0.7, 0.075},
//                {0.075, 0.075, 0.075, 0.075, 0.2}};
//        B = new double[][]{{0.125, 0.125, 0.125, 0.125, 0.0, 0.125, 0.125, 0.125, 0.125},
//                {0.36, 0.04, 0.36, 0.04, 0.04, 0.04, 0.04, 0.04, 0.04},
//                {0.016, 0.016, 0.016, 0.225, 0.02, 0.225, 0.016, 0.45, 0.016},
//                {0.15, 0.15, 0.15, 0.04, 0.02, 0.04, 0.15, 0.15, 0.15},
//                {0.1125, 0.1125, 0.1125, 0.1125, 0.1, 0.1125, 0.1125, 0.1125, 0.1125}};
        A = new double[][]{{0.2024, 0.2142, 0.1945, 0.1924, 0.1965},
                {0.1923, 0.2351, 0.2, 0.1904, 0.1822},
                {0.2125, 0.1925, 0.1974, 0.2023, 0.1953},
                {0.2042, 0.2, 0.1989, 0.1962, 0.2007},
                {0.1863, 0.1987, 0.1929, 0.2219, 0.2002}};
        B = new double[][] {{0.125, 0.125, 0.125, 0.125, 0.0, 0.125, 0.125, 0.125, 0.125},
                {0.36, 0.04, 0.36, 0.04, 0.04, 0.04, 0.04, 0.04, 0.04},
                {0.016, 0.016, 0.016, 0.225, 0.02, 0.225, 0.016, 0.45, 0.016},
                {0.15, 0.15, 0.15, 0.04, 0.02, 0.04, 0.15, 0.15, 0.15},
                {0.1125, 0.1125, 0.1125, 0.1125, 0.1, 0.1125, 0.1125, 0.1125, 0.1125}};

        pi = new double[]{0.2042, 0.19453, 0.2, 0.20345, 0.19782};
//        int sum = 0;
//        for(int i = 0; i < N-1; i++) {
//            pi[i] = 1.0/N+(Math.random()-0.5)/100;
//            sum += pi[i];
//        }
//        pi[N-1] = 1 - sum;
        for (int i = 0; i < observation.length; i++) {
            observation[i] = 0;
        }
    }

    public void modelTrain(Bird bird) {
        T = 0;
        for (int i = 0; i < bird.getSeqLength(); i++) {
            if (bird.getObservation(i) != -1) {
                observation[i] = bird.getObservation(i);
                T++;
            } else {
                break;
            }
        }

        int maxIters = 100;
        int iters = 0;
        Double oldLogProb = -1e5;
        Double logProb = -1e4;

        while (iters < maxIters && (logProb - oldLogProb) > 0.0009)  //0.00008312)
        {
            iters++;
            oldLogProb = logProb;
            logProb = BaumWelch(observation);
        }
    }

    private double BaumWelch(Integer[] emissionsArr) {
//        int T = emissionsArr.length;
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
                if (alpha[t][i] < 2e-10) {
                    alpha[t][i] = 2e-10;
                }
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
                if (beta[t][i] < 2e-10) {
                    beta[t][i] = 2e-10;
                }
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

    public int[] getObserSeq(Bird bird)
    {
        int seqNum = bird.getSeqLength();
        int[] obserSeq = new int[seqNum];
        for(int i = 0; i < seqNum; i++)
            obserSeq[i] = bird.getObservation(i);
        return obserSeq;
    }

    public void trainModel(Bird bird)
    {
        int[] obserSeq = getObserSeq(bird);
        int seqNum = obserSeq.length;
        int tranRowNum = A.length;
        int tranColNum = A[0].length;
        int iniColNum = pi.length;
        int emiColNum = B[0].length;
        //int emiRowNum = tranColNum;
//        //initial observation sequence

        double preLogProb = 0.0;
        double logProb = 0.0;
        int loop = 0;
        while(true) //&& loop < 80)
        {
            loop++;
            double[][] alfa = new double[seqNum][tranRowNum];
            double[][] beta = new double[seqNum][tranRowNum];
            double[] alfaScale = new double[seqNum];
            //initial alfa and beta matrix
            alfaScale[0] = 0;
            for(int i = 0; i < iniColNum; i++)
            {
                alfa[0][i] = pi[i] * B[i][obserSeq[0]];
                alfaScale[0] += alfa[0][i];
                //System.out.print(alfa[0][i] + " ");
                //beta[seqNum-1][i] = 1;
            }
            //scale alfa0(i)
            if(alfaScale[0] != 0)
                for(int i = 0; i < iniColNum; i++)
                    alfa[0][i] = alfa[0][i] / alfaScale[0];

            //calculate alfa
            for(int t = 1; t <= seqNum-1; t++) //time series
            {
                alfaScale[t] = 0;
                for(int i = 0; i < tranColNum; i++)
                {
                    alfa[t][i] = 0;
                    for(int j = 0; j < tranRowNum; j++)
                    {
                        alfa[t][i] += (alfa[t-1][j] * A[j][i]);
                    }
                    //System.out.println("temp = " + temp);
                    alfa[t][i] *=  B[i][obserSeq[t]];
                    alfaScale[t] += alfa[t][i];
                }
                //scale alfa
                if(alfaScale[t] != 0)
                    for(int i = 0; i < tranColNum; i++)
                        alfa[t][i] = alfa[t][i] / alfaScale[t];

            }
            //scale beta[T][i]
            if(alfaScale[seqNum-1] != 0)
                for(int i = 0; i < tranColNum; i++)
                    beta[seqNum-1][i] = 1.0 / alfaScale[seqNum-1];

            for(int t = 1; t <= seqNum-1; t++) //time series
            {
                for(int i = 0; i < tranColNum; i++)
                {
                    for(int j = 0; j < tranRowNum; j++)
                    {
                        beta[seqNum-t-1][i] += (A[i][j] * beta[seqNum-t][j] * B[j][obserSeq[seqNum-t]]);
                    }
                    if(alfaScale[seqNum-t-1] != 0)
                        beta[seqNum-t-1][i] = beta[seqNum-t-1][i] / alfaScale[seqNum-t-1];
                }

            }
            //double alfaTSum = 0.0;
//            for(int i = 0; i < tranColNum; i++)
//                alfaTSum += alfa[seqNum-1][i];

            double[][][] digama = new double[seqNum][tranRowNum][tranColNum];
            double[][] gama = new double[seqNum][tranRowNum];
            //calculate digama and gama
            for(int t = 0; t <= seqNum-2; t++){
                for(int i = 0; i < tranRowNum; i++)
                {
                    gama[t][i] = 0;
                    for(int j = 0; j < tranColNum; j++)
                    {
                        digama[t][i][j] = alfa[t][i] * A[i][j] * B[j][obserSeq[t+1]] * beta[t+1][j];
                        gama[t][i] += digama[t][i][j];
                    }
                }
            }
            for(int i = 0; i <= tranColNum-1; i++)
                gama[seqNum-1][i] = alfa[seqNum-1][i];

            //update initial pai
            for(int i = 0; i < iniColNum; i++)
                pi[i] = gama[0][i];

            //update A matrix
            for(int i = 0; i < tranRowNum; i++)
            {
                double gamaSum = 0.0;
                for(int t = 0; t <= seqNum-2; t++)
                {
                    gamaSum += gama[t][i];
                }
                for(int j = 0; j < tranColNum; j++)
                {
                    double digamaSum = 0.0;
                    for(int t = 0; t <= seqNum-2; t++)
                    {
                        digamaSum += digama[t][i][j];
                    }
                    if(gamaSum != 0)
                        A[i][j] = digamaSum / gamaSum;
                }
            }
            //update B matrix
            for(int i = 0; i < tranColNum; i++)
            {
                double gamaSum = 0.0;
                for(int t = 0; t <= seqNum-1; t++)
                {
                    gamaSum += gama[t][i];
                }
                for(int j = 0; j < emiColNum; j++)
                {
                    double gamaSumOnk = 0.0;
                    for(int t = 0; t <= seqNum-1; t++)
                    {
                        if(j == obserSeq[t])
                            gamaSumOnk += gama[t][i];
                    }
                    if(gamaSum != 0)
                        B[i][j] = gamaSumOnk / gamaSum;
                }
            }
            // check whether it is converge or not.


            logProb = 0.0;
            for(int t = 0; t < seqNum; t++)
            {
                logProb += Math.log10(alfaScale[t]);
            }
            logProb = -logProb;
            if(Math.abs(logProb-preLogProb) < 0.008)
                break;
            preLogProb = logProb;
            //System.err.println("logProb = " + logProb);

        }


    }

    //using forward-pass algorithm to current emission and hidden state distribution.
    public double[] getCurrState(Bird bird)
    {
        int[] obserSeq = getObserSeq(bird);
        int seqNum = obserSeq.length;
        double[][] alfa = new double[seqNum][A.length];
        double sum = 0.0;
        //initial alfa matrix
        for(int i = 0; i < A.length; i++)
        {
            alfa[0][i] = pi[i] * B[i][obserSeq[0]];
            sum += alfa[0][i];
        }
        if(sum != 0)
            for(int i = 0; i < N; i++)
                alfa[0][i] = alfa[0][i] / sum;

        for(int i = 1; i <= seqNum-1; i++)
        {
            sum = 0.0;
            for(int k = 0; k < A.length; k++)
            {
                double temp = 0.0;
                for(int j = 0; j < alfa[0].length; j++)
                {
                    //calculate alfa(t-1) * transMix
                    temp += (alfa[i-1][j] * A[j][k]);
                }
                alfa[i][k] = temp * B[k][obserSeq[i]];
                sum += alfa[i][k];
            }
            if(sum != 0)
                for(int k = 0; k < N; k++)
                    alfa[i][k] = alfa[i][k] / sum;

        }
        return alfa[seqNum-1];

    }
    public double[] getNextEmiState(double[] currentState)
    {
        double[] nextEmiProb = new double[M];
        for(int i = 0; i < N; i++)
        {
            for(int j = 0; j < N; j++)
            {
                for(int k = 0; k < M; k++)
                {
                    //把从j转移到i的对应所有某movement存起来。
                    nextEmiProb[k] += currentState[j] * A[j][i] * B[i][k];
                }
            }
        }
        return normalizeVector(nextEmiProb);
    }

    private double[] normalizeVector(double[] vector)
    {
        double sum = 0.0;
        for(int i = 0; i < vector.length; i++)
        {
            sum += vector[i];
        }
        if(sum == 0.0)
            return vector;

        for(int i = 0; i < vector.length; i++)
        {
            vector[i] = vector[i] / sum;
        }
        return vector;
    }




    public double calculateProb(Bird bird)
    {
        int[] obserSeq = getObserSeq(bird);
        int seqNum = obserSeq.length;
        double[][] alfa = new double[seqNum][N];
        double[] alfaScale = new double[seqNum];
//        System.err.println("-----A-----");
//        printMatrix(A);
//        System.err.println("-----B-----");
//        printMatrix(B);

        alfaScale[0] = 0;
        //initial alfa matrix
        for(int i = 0; i < N; i++)
        {
            alfa[0][i] = pi[i] * B[i][obserSeq[0]];
            alfaScale[0] += alfa[0][i];
        }

        for(int i = 0; i < N; i++)
            alfa[0][i] = alfa[0][i] / alfaScale[0];

        for(int i = 1; i <= seqNum-1; i++)
        {
            alfaScale[i] = 0;
            for(int k = 0; k < A.length; k++)
            {
                double temp = 0.0;
                for(int j = 0; j < alfa[0].length; j++)
                {
                    //calculate alfa(t-1) * transMix
                    temp += (alfa[i-1][j] * A[j][k]);
                }
                alfa[i][k] = temp * B[k][obserSeq[i]];
                alfaScale[i] += alfa[i][k];
            }
            //scale alfa
            for(int v = 0; v < N; v++)
                alfa[i][v] = alfa[i][v] / alfaScale[i];
        }
        double seqProb = 0.0;
        for(int i = 0; i < alfa[0].length; i++)
        {
            //System.out.print(alfa[seqNum-1][i]+" ");
            seqProb += alfa[seqNum-1][i];
        }
        return seqProb;
    }

    public double getProb(Bird bird) {
        T = 0;
        for (int i = 0; i < bird.getSeqLength(); i++) {
            if (bird.getObservation(i) != -1) {
                observation[i] = bird.getObservation(i);
                T++;
            } else {
                break;
            }
        }

        double[][] initPossibility = new double[1][N];
        for (int i = 0; i < A.length; i++) {
            initPossibility[0][i] = observation[i] * pi[i];
        }

        double[][] finalPossibility = initPossibility;
        for (int i = 1; i < T; i++) {
            finalPossibility = forwardOneStep(finalPossibility, observation[i]);
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
