public class HMMmodel {
    private double[][] tranMatrix;
    private double[][]  emiMatrix;
    private double[] iniMatrix;
    //private int[] obserSeq;
    private  static final int  stateNum = 5;
    private static final int emiColNum = 9;


    public HMMmodel()
    {

        iniMatrix = new double[stateNum];
        tranMatrix = new double[stateNum][stateNum];
        emiMatrix = new double[stateNum][emiColNum];

        for (int i = 0; i < stateNum; ++i) {
            for (int j = 0; j < stateNum; ++j) {
                this.tranMatrix[i][j] = 1000 + Math.random()*10000;
//        this.A[i][j] = Math.random()*(0.9-0.1)+0.1;
                // A[i][j] = 3 + Math.random()*(0.09-0.01)+0.01;
                //  if (i == j)
                //   A[i][j] += 100 * numberOfStates;
            }
        }

        this.tranMatrix= normalize(tranMatrix);


        for (int i = 0; i < stateNum; ++i) {
            for (int j = 0; j < emiColNum; ++j) {
                this.emiMatrix[i][j] = 1000 + Math.random()*500;
//        this.B[i][j] = Math.random()*(0.9-0.1)+0.1;
                // B[i][j] = 3 + Math.random()*(0.09-0.01)+0.01;
                //if (i == j)
                // B[i][j] += 100 * numberOfStates;
                //if(j >= numberOfStates)
                // B[i][j] += 10;
            }
        }

        this.emiMatrix = normalize(emiMatrix);


        //initialize A,B,pai
//        tranMatrix = new double[][] {{0.8, 0.05, 0.05, 0.05, 0.05},
//                {0.075, 0.7, 0.075, 0.075, 0.075},
//                {0.075, 0.075, 0.7, 0.075, 0.075},
//                {0.075, 0.075, 0.075, 0.7, 0.075},
//                {0.075, 0.075, 0.075, 0.075, 0.7}};
//        emiMatrix = new double[][] {{0.125, 0.125, 0.125, 0.125, 0.0, 0.125, 0.125, 0.125, 0.125},
//                {0.36, 0.04, 0.36, 0.04, 0.04, 0.04, 0.04, 0.04, 0.04},
//                {0.016, 0.016, 0.016, 0.225, 0.02, 0.225, 0.016, 0.45, 0.016},
//                {0.15, 0.15, 0.15, 0.04, 0.02, 0.04, 0.15, 0.15, 0.15},
//                {0.1125, 0.1125, 0.1125, 0.1125, 0.1, 0.1125, 0.1125, 0.1125, 0.1125}};

        for(int i = 0; i < stateNum-1; i++) {
            iniMatrix[i] = 1.0/stateNum+(Math.random()-0.5)/100;
        }


    }
    public double[][] normalize(double m[][]) {
        double[][] m2 = new double[m.length][m[0].length];

        for (int row = 0; row < m.length; row++) {
            double sum = sum(m[row]);
            if (sum != 0)
                for (int col = 0; col < m[row].length; col++) {
                    m2[row][col] = m[row][col] / sum;
                }
        }

        return m2;

    }

    public static double[] normalize(double[] a) {
        double sum =0.0;
        double[] a2 = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            sum += a[i];
        }
        //System.err.println("SUM "+ sum);
        for (int i = 0; i < a.length; i++) {
            a2[i] = a[i] * (1.0 / sum);
        }

        return a2;
    }

    public double sum(double[] prob) {
        double sum = 0;
        for (double d : prob)
            sum += d;

        return sum;
    }

    public void trainModel(Bird bird)
    {
        int[] obserSeq = getObserSeq(bird);
        int seqNum = obserSeq.length;
        int tranRowNum = tranMatrix.length;
        int tranColNum = tranMatrix[0].length;
        int iniColNum = iniMatrix.length;
        int emiColNum = emiMatrix[0].length;
        //int emiRowNum = tranColNum;
        obserSeq = new int[bird.getSeqLength()];
//        //initial observation sequence
        for(int i = 0; i <= bird.getLastObservation(); i++)
            obserSeq[i] = bird.getObservation(i);
        double preLogProb = 0.0;
        double logProb = 0.0;
        while(true)
        {
            double[][] alfa = new double[seqNum][tranRowNum];
            double[][] beta = new double[seqNum][tranRowNum];
            double[] alfaScale = new double[seqNum];
            //initial alfa and beta matrix
            alfaScale[0] = 0;
            for(int i = 0; i < iniColNum; i++)
            {
                alfa[0][i] = iniMatrix[i] * emiMatrix[i][obserSeq[0]];
                alfaScale[0] += alfa[0][i];
                //System.out.print(alfa[0][i] + " ");
                //beta[seqNum-1][i] = 1;
            }
            //scale alfa0(i)
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
                        alfa[t][i] += (alfa[t-1][j] * tranMatrix[j][i]);
                    }
                    //System.out.println("temp = " + temp);
                    alfa[t][i] *=  emiMatrix[i][obserSeq[t]];
                    alfaScale[t] += alfa[t][i];
                }
                //scale alfa
                for(int i = 0; i < tranColNum; i++)
                    alfa[t][i] = alfa[t][i] / alfaScale[t];

            }
            //scale beta[T][i]
            for(int i = 0; i < tranColNum; i++)
                beta[seqNum-1][i] = 1.0 / alfaScale[seqNum-1];

            for(int t = 1; t <= seqNum-1; t++) //time series
            {
                for(int i = 0; i < tranColNum; i++)
                {
                    for(int j = 0; j < tranRowNum; j++)
                    {
                        beta[seqNum-t-1][i] += (tranMatrix[i][j] * beta[seqNum-t][j] * emiMatrix[j][obserSeq[seqNum-t]]);
                    }
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
                        digama[t][i][j] = alfa[t][i] * tranMatrix[i][j] * emiMatrix[j][obserSeq[t+1]] * beta[t+1][j];
                        gama[t][i] += digama[t][i][j];
                    }
                }
            }
            for(int i = 0; i <= tranColNum-1; i++)
                gama[seqNum-1][i] = alfa[seqNum-1][i];

            //update initial pai
            for(int i = 0; i < iniColNum; i++)
                iniMatrix[i] = gama[0][i];

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
                    tranMatrix[i][j] = digamaSum / gamaSum;
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
                    emiMatrix[i][j] = gamaSumOnk / gamaSum;
                }
            }
            // check whether it is converge or not.

            preLogProb = logProb;
            logProb = 0.0;
            for(int t = 0; t < seqNum; t++)
            {
                logProb += Math.log10(alfaScale[t]);
            }
            logProb = -logProb;
            if(Math.abs(preLogProb - logProb) < 0.1)
                break;
            System.err.println("logProb = " + logProb);

        }
//        System.out.print(tranColNum + " " + tranRowNum + " ");
//        for(int i = 0; i < tranColNum; i++){
//            for(int j = 0; j < tranColNum; j++)
//            {
//                System.out.print(tranMatrix[i][j] + " ");
//            }
//        }
//        System.out.println();
//        System.out.print(emiRowNum + " " + emiColNum + " ");
//        for(int i = 0; i < emiRowNum; i++){
//            for(int j = 0; j < emiColNum; j++)
//            {
//                System.out.print(emiMatrix[i][j] + " ");
//            }
//        }

    }

    public int[] getObserSeq(Bird bird)
    {
        int seqNum = bird.getSeqLength();
        int[] obserSeq = new int[seqNum];
        for(int i = 0; i < seqNum; i++)
            obserSeq[i] = bird.getObservation(i);
        return obserSeq;
    }
    public int[] getObserSeq(Bird bird, int movement)
    {
        int seqNum = bird.getSeqLength() + 1;
        int[] obserSeq = new int[seqNum];
        for(int i = 0; i < obserSeq.length-1; i++)
            obserSeq[i] = bird.getObservation(i);
        obserSeq[seqNum-1] = movement;
        return obserSeq;
    }

    public double calculateProb(int[] obserSeq)
    {
        //int[] obserSeq = getObserSeq(bird);
        int seqNum = obserSeq.length;
        double[][] alfa = new double[seqNum][stateNum];
        double[] alfaScale = new double[seqNum];

        alfaScale[0] = 0;
        //initial alfa matrix
        for(int i = 0; i < stateNum; i++)
        {
            alfa[0][i] = iniMatrix[i] * emiMatrix[i][obserSeq[0]];
            alfaScale[0] += alfa[0][i];
        }

        for(int i = 0; i < stateNum; i++)
            alfa[0][i] = alfa[0][i] / alfaScale[0];

        for(int i = 1; i <= seqNum-1; i++)
        {
            alfaScale[i] = 0;
            for(int k = 0; k < tranMatrix.length; k++)
            {
                double temp = 0.0;
                for(int j = 0; j < alfa[0].length; j++)
                {
                    //calculate alfa(t-1) * transMix
                    temp += (alfa[i-1][j] * tranMatrix[j][k]);
                }
                alfa[i][k] = temp * emiMatrix[k][obserSeq[i]];
                alfaScale[i] += alfa[i][k];
            }
            //scale alfa
            for(int v = 0; v < stateNum; v++)
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

    public int[] predictState(int[] obserSeq)
    {
        int tranRowNum = tranMatrix.length;
        int tranColNum = tranMatrix[0].length;
        int iniColNum = iniMatrix.length;
        //int emiColNum = emiMatrix[0].length;
        int seqNum = obserSeq.length;

        double[][] delta = new double[seqNum][tranRowNum];
        int[][] tracks = new int[seqNum][tranRowNum];
        double max = 0.0;
        int pt = 0;
        //initial delta and backward pointer
        for(int i = 0; i < iniColNum; i++)
        {
            delta[0][i] = iniMatrix[i] * emiMatrix[i][obserSeq[0]];
//
        }
        //delta[0] = max;
        //tracks[0] = pt;


        for(int i = 1; i < seqNum; i++)
        {
            for(int j = 0; j < tranColNum; j++)
            {
                max = 0.0;
                pt = 0;
                for(int k = 0; k < tranRowNum; k++)
                {
                    double temp = delta[i-1][k] * tranMatrix[k][j] * emiMatrix[j][obserSeq[i]];
                    if(temp > max)
                    {
                        max = temp;
                        pt = k;
                    }
                }
                delta[i][j] = max;
                tracks[i][j] = pt;
            }
        }
        max = 0.0;
        int lastPoint = 0;
        for(int i = 0; i < tranColNum; i++)
            if(delta[seqNum-1][i] > max){
                max = delta[seqNum-1][i];
                //back track starts with lastPoint.
                lastPoint = i;
            }
        //System.out.print(lastPoint+ " ");
        int tmp = lastPoint;
        int[] printArray = new int[seqNum];
        printArray[seqNum-1] = lastPoint;
        for(int i = seqNum-1; i >= 1; i--)
        {
            tmp = tracks[i][tmp];
            printArray[i-1] = tmp;
        }
        return printArray;
    }

    public int nextMovementPredict(int[] hiddenStates)
    {
        int tranColNum = tranMatrix[0].length;
        int emiColNum = emiMatrix[0].length;

        int preHiddenState = hiddenStates[hiddenStates.length-1];
        double max = 0.0;
        int state = 0;
        for(int i = 0; i < tranColNum; i++)
        {
            if(tranMatrix[preHiddenState][i] > max)
            {
                max = tranMatrix[preHiddenState][i];
                state = i;
            }
        }
        double[] output = new double[emiColNum];
        for(int i = 0; i < emiColNum; i++)
        {
            for(int j = 0; j < tranColNum; j++)
            {
                output[i] += tranMatrix[state][j] * emiMatrix[j][i];
            }
        }
        max = 0.0;
        int index = 0;
        for(int i = 0; i < output.length; i++)
        {
            if(output[i] > max)
            {
                max = output[i];
                index = i;
            }
        }
        return index;
    }

    public double[][] getTranMatrix()
    {
        return tranMatrix;
    }
    public double[][] getEmiMatrix()
    {
        return emiMatrix;
    }
    public double[] getIniMatrix()
    {
        return iniMatrix;
    }

}
