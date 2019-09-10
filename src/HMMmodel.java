public class HMMmodel {
    private double[][] tranMatrix;
    private double[][]  emiMatrix;
    private double[] iniMatrix;
    private int[] obserSeq;

    public HMMmodel(int tranRowNum, int tranColNum, int emiRowNum, int emiColNum, Bird bird)
    {
        //tranMatrix = new double[tranRowNum][tranColNum];
        //emiMatrix = new double[emiRowNum][emiColNum];
        iniMatrix = new double[tranColNum];
        obserSeq = new int[bird.getSeqLength()];
        //initial observation sequence
        for(int i = 0; i <= bird.getLastObservation(); i++)
            obserSeq[i] = bird.getObservation(i);

        //initialize A,B,pai
        tranMatrix = new double[][] {{0.8, 0.05, 0.05, 0.05, 0.05},
                {0.075, 0.7, 0.075, 0.075, 0.075},
                {0.075, 0.075, 0.7, 0.075, 0.075},
                {0.075, 0.075, 0.075, 0.7, 0.075},
                {0.075, 0.075, 0.075, 0.075, 0.7}};
        emiMatrix = new double[][] {{0.125, 0.125, 0.125, 0.125, 0.0, 0.125, 0.125, 0.125, 0.125},
                {0.36, 0.04, 0.36, 0.04, 0.04, 0.04, 0.04, 0.04, 0.04},
                {0.016, 0.016, 0.016, 0.225, 0.02, 0.225, 0.016, 0.45, 0.016},
                {0.15, 0.15, 0.15, 0.04, 0.02, 0.04, 0.15, 0.15, 0.15},
                {0.1125, 0.1125, 0.1125, 0.1125, 0.1, 0.1125, 0.1125, 0.1125, 0.1125}};

        for(int i = 0; i < tranColNum-1; i++) {
            iniMatrix[i] = 1.0/tranColNum+(Math.random()-0.5)/100;
        }
//        for(int i = 0; i < tranRowNum; i++)
//        {
//            iniMatrix[i] = 1.0 / tranRowNum;
//            for(int j = 0; j < tranColNum; j++)
//            {
//                tranMatrix[i][j] = 1.0 / tranColNum;
//            }
//            for(int k = 0; k < emiColNum; k++)
//                emiMatrix[i][k] = 1.0 / emiColNum;
//        }
    }

    public void trainModel()
    {
        int seqNum = obserSeq.length;
        int tranRowNum = tranMatrix.length;
        int tranColNum = tranMatrix[0].length;
        int iniColNum = iniMatrix.length;
        int emiColNum = emiMatrix[0].length;
        int emiRowNum = tranColNum;

        for(int loop = 0; loop < 50; loop++)
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
            //calculate beta
//            for(int t = seqNum-2; t >= 0; t--)
//            {
//                for(int i = 0; i < tranColNum; i++)
//                {
//                    beta[t][i] = 0;
//                    for(int j = 0; j < tranColNum; j++)
//                    {
//                        beta[t][i] = beta[t][i] + tranMatrix[i][j] * emiMatrix[j][obserSeq[t+1]] * beta[t+1][j];
//                    }
//                    beta[t][i] = beta[t][i] / alfaScale[t];
//
//                }
//            }
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
//            double logProb = 0;
//            for(int t = 0; t < seqNum; t++)
//            {
//                logProb += Math.log10(alfaScale[t]);
//            }
//            logProb = -logProb;
//            System.out.println(logProb);

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

    public int[] predictState()
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
