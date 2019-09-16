import java.util.ArrayList;

class Player {
    public static final Action cDontShoot = new Action(-1, -1);
    private ArrayList<HMM>[] hmms = new ArrayList[Constants.COUNT_SPECIES];
    private static int timer = 0;
    private int[] Guess;
    private boolean flag = false;
    private int currentRound = 0;
    private int hitNum = 0;

    private int currentRound2 = 0;

    public Player() {
        for (int i = 0; i < Constants.COUNT_SPECIES; i++) {
            ArrayList<HMM> hmmArr = new ArrayList<>();
            hmms[i] = hmmArr;
        }
    }

    /**
     * Shoot!
     *
     * This is the function where you start your work.
     *
     * You will receive a variable pState, which contains information about all
     * birds, both dead and alive. Each bird contains all past moves.
     *
     * The state also contains the scores for all players and the number of
     * time steps elapsed since the last time this function was called.
     *
     * @param pState the GameState object with observations etc
     * @param pDue time before which we must have returned
     * @return the prediction of a bird we want to shoot at, or cDontShoot to pass
     */
    public Action shoot(GameState pState, Deadline pDue) {
        /*
         * Here you should write your clever algorithms to get the best action.
         * This skeleton never shoots.
         */
        //return cDontShoot;
        timer++;

        if(pState.getRound() > currentRound)
        {
            currentRound = pState.getRound();
//            Guess = this.guess(pState, pDue);
            timer = 0;
//            for(int species:Guess)
//                System.err.print("species:" + species+" ");
//            System.err.println();
        }

        if(timer < 68)//100 - pState.getNumBirds())
            return cDontShoot;

        // System.err.println("round = " + pState.getRound());
        if(pState.getRound() == 0)
            return cDontShoot;

        double maxProbAll = 0.0;
        int bestMovement = 0;
        int birdno = 0;


        for(int i = 0; i < pState.getNumBirds(); i++)
        {
            //System.err.println("---in the loop---");
            Bird bird = pState.getBird(i);
            if(bird.isDead())
                continue;

            //int[] lGuess = this.guess(pState, pDue);

//            int species = Guess[i];
//            System.err.println("-----bird sequences-----");
//            for(int n = 0; n < bird.getSeqLength(); n++)
//                System.err.print(bird.getObservation(n) + " ");
            int species = getSpecies(bird);
            if(species == Constants.SPECIES_BLACK_STORK)
                continue;

            HMM birdHmm = new HMM();
            birdHmm.trainModel(bird);

            double[] currentStates = birdHmm.getCurrState(bird);
            double[] nextEmiStates = birdHmm.getNextEmiState(currentStates);

            double maxProb = 0.0;
            int movement = 0;
            for(int j = 0; j < nextEmiStates.length; j++)
            {
                if(maxProb < nextEmiStates[j])
                {
                    maxProb = nextEmiStates[j];
                    movement = j;
                }
            }

            if(maxProbAll < maxProb)
            {
                maxProbAll = maxProb;
                bestMovement = movement;
                birdno = i;
            }


        }
        //System.err.println("maxProbAll = " + maxProbAll);
        if(maxProbAll >= 0.7)
            return new Action(birdno, bestMovement);


        return cDontShoot;

        // return new Action(0, MOVE_RIGHT);
    }

    public int getSpecies(Bird bird)
    {
        int[] obserSeq = new int[bird.getSeqLength()];
        for(int i = 0; i < obserSeq.length; i++)
        {
            obserSeq[i] = bird.getObservation(i);
        }
        double maxProb = 0.0;
        double prob = 0.0;
        int species = 0;
        for(int i = 0; i < hmms.length; i++)
        {
            for(int j = 0; j < hmms[i].size(); j++)
            {
                prob = hmms[i].get(j).calculateProb(bird);
                if(prob > maxProb)
                {
                    maxProb = prob;
                    species = i;
                }
            }
        }
        if(maxProb >= 0.75)
            return species;
        return 5;
    }

    /**
     * Guess the species!
     * This function will be called at the end of each round, to give you
     * a chance to identify the species of the birds for extra points.
     *
     * Fill the vector with guesses for the all birds.
     * Use SPECIES_UNKNOWN to avoid guessing.
     *
     * @param pState the GameState object with observations etc
     * @param pDue time before which we must have returned
     * @return a vector with guesses for all the birds
     */
    public int[] guess(GameState pState, Deadline pDue) {
        /*
         * Here you should write your clever algorithms to guess the species of
         * each bird. This skeleton makes no guesses, better safe than sorry!
         */

        int[] lGuess = new int[pState.getNumBirds()];
        for (int i = 0; i < pState.getNumBirds(); ++i) {
            lGuess[i] = Constants.SPECIES_PIGEON;
        }

        if (pState.getRound() == 0) {
            return lGuess;
        } else {
            int birdAmount = pState.getNumBirds();

            for (int i = 0; i < birdAmount; i++) {
                double maxPro = 0.0;
                int speciesGuess = Constants.SPECIES_UNKNOWN;
                Bird bird = pState.getBird(i);

                for (int j = 0; j < Constants.COUNT_SPECIES; j++) {
                    for (int k = 0; k < hmms[j].size(); k++) {
                        double possibility = hmms[j].get(k).getProb(bird);
                        if (possibility > maxPro) {
                            maxPro = possibility;
                            speciesGuess = j;
                        }
                    }
                }
                lGuess[i] = speciesGuess;
            }
            return lGuess;
        }
    }

    /**
     * If you hit the bird you were trying to shoot, you will be notified
     * through this function.
     *
     * @param pState the GameState object with observations etc
     * @param pBird the bird you hit
     * @param pDue time before which we must have returned
     */
    public void hit(GameState pState, int pBird, Deadline pDue) {
        System.err.println("HIT BIRD!!!");
//        hitNum++;
//        if(pState.getRound() > currentRound2)
//        {
//           // System.err.println("round " + currentRound2 + " hit " +hitNum + " birds");
//            currentRound2 = pState.getRound();
//            hitNum = 0;
//        }
    }

    /**
     * If you made any guesses, you will find out the true species of those
     * birds through this function.
     *
     * @param pState   the GameState object with observations etc
     * @param pSpecies the vector with species
     * @param pDue     time before which we must have returned
     */
    public void reveal(GameState pState, int[] pSpecies, Deadline pDue) {
//        System.err.print("SPECIES: ");
//        for (int i = 0; i < pSpecies.length; i++) {
//            System.err.print(pSpecies[i] + " ");
//        }
//        System.err.println("");

        for (int i = 0; i < pState.getNumBirds(); i++) {
            if (pSpecies[i] != Constants.SPECIES_UNKNOWN) {
                int species = pSpecies[i];
                HMM hmm = new HMM();
                hmm.modelTrain(pState.getBird(i));
                if (hmms[species].size() < 20)
                    hmms[species].add(hmm);
            }
        }
    }

}
