import java.util.ArrayList;

class Player {
    public static final Action cDontShoot = new Action(-1, -1);
    private ArrayList<HMM>[] hmms = new ArrayList[Constants.COUNT_SPECIES];
    private static int timer = 0;
    private int[] Guess;
    private boolean flag = false;
    private int currentRound = 0;

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
        timer++;
        if(timer < 70)
        {
            return cDontShoot;
        }

        if(timer >= 100)
            timer = 0;

        if(pState.getRound() == 0)
            return cDontShoot;

        if(pState.getRound() > currentRound)
        {
            currentRound = pState.getRound();
            Guess = this.guess(pState, pDue);
//            for(int species:Guess)
//                System.err.print("species:" + species+" ");
//            System.err.println();
        }


        double maxProbAll = 0.0;
        int bestMovement = 0;
        int birdno = 0;


        for(int i = 0; i < pState.getNumBirds(); i++)
        {
            //System.err.println("---in the loop---");
            Bird bird = pState.getBird(i);
            if(bird.isDead())
                continue;

            int species = Guess[i];
//            if(species == Constants.SPECIES_BLACK_STORK)
//                continue;

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

        return new Action(birdno, bestMovement);

//        for(int i = 0; i < pState.getNumBirds(); i++)
//        {
//            Bird bird = pState.getBird(i);
//            if(bird.isDead() || lGuess[i] == Constants.SPECIES_BLACK_STORK)
//                continue;
//            double maxProb = 0.0;
//            double total = 0.0;
//            int movement = 0;
//            for(int k = 0; k < Constants.COUNT_MOVE; k++)
//            {
//                int guessNum = lGuess[i];
//                if(guessNum == -1)
//                    guessNum = (int)Math.random() * 5;
//                for(int j = 0; j < hmms[guessNum].size(); j++)
//                {
//                    total += hmms[guessNum].get(j).getProb(bird, k);
//                }
//                if(total > maxProb)
//                {
//                    maxProb = total;
//                    movement = k;
//                }
//            }
//            return new Action(i, movement);
//
//        }


        //return cDontShoot;

        // return new Action(0, MOVE_RIGHT);
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
            lGuess[i] = Constants.SPECIES_SNIPE;
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
