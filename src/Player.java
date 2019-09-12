import java.util.ArrayList;

class Player {

    public static final Action cDontShoot = new Action(-1, -1);
    private ArrayList<HMM>[] hmms = new ArrayList[Constants.COUNT_SPECIES];
    private ArrayList<boolean[]> birdsHasShootArr = new ArrayList<>();

    public Player() {
        for (int i = 0; i < Constants.COUNT_SPECIES; i++) {
            ArrayList<HMM> hmmArr = new ArrayList<>();
            hmms[i] = hmmArr;
        }
    }

    /**
     * Shoot!
     * <p>
     * This is the function where you start your work.
     * <p>
     * You will receive a variable pState, which contains information about all
     * birds, both dead and alive. Each bird contains all past moves.
     * <p>
     * The state also contains the scores for all players and the number of
     * time steps elapsed since the last time this function was called.
     *
     * @param pState the GameState object with observations etc
     * @param pDue   time before which we must have returned
     * @return the prediction of a bird we want to shoot at, or cDontShoot to pass
     */
    public Action shoot(GameState pState, Deadline pDue) {
        /*
         * Here you should write your clever algorithms to get the best action.
         * This skeleton never shoots.
         */

        // For every bird in every round, we shoot it once.
        // This is because if we miss one bird, we lose one point.
        // If we shoot it for the second time, we may hit it and earn one point,
        // but we may still miss it and lose one point again.
        // If so, we will lose 2 points for the same bird.
        // This is worse than we don't shoot it for the second time.
//        int currentRound = pState.getRound();
//        if (birdsHasShootArr.size() < currentRound + 1) {
//            boolean[] birdsShoot = new boolean[pState.getNumBirds()];
//            for (int i = 0; i < birdsShoot.length; i++) {
//                birdsShoot[i] = true;
//            }
//            birdsHasShootArr.add(birdsShoot);
//        }
//
//        int shootRound = 70;
//        if (pState.getBird(0).getSeqLength() < shootRound) {
//            return cDontShoot;
//        }
//
//        int[] lGuess = this.guess(pState, pDue);
//        double maxProb = 0.0;
//        int movement = Constants.MOVE_DEAD;
//        int birdNum = Constants.SPECIES_UNKNOWN;
//
//        for (int i = 0; i < pState.getNumBirds(); i++) {
//
//        }
//
//        for (int i = 0; i < pState.getNumBirds(); i++) {
//            Bird bird = pState.getBird(i);
//
//            if (!bird.isAlive()) {
//                birdsHasShootArr.get(currentRound)[i] = false;
//            }
//
//            if (lGuess[i] != Constants.SPECIES_UNKNOWN
//                    && lGuess[i] != Constants.SPECIES_BLACK_STORK
//                    && birdsHasShootArr.get(currentRound)[i]
//                    && bird.isAlive()) {
//                ArrayList<HMM> hmmArray = hmms[lGuess[i]];
//
//                for (int j = 0; j < hmmArray.size(); j++) {
//                    for (int k = 0; k < Constants.COUNT_MOVE; k++) {
//                        double possibility = hmmArray.get(j).getProb(bird, k);
//                        if (possibility > maxProb) {
//                            maxProb = possibility;
//                            movement = k;
//                            birdNum = i;
//                        }
//                    }
//                }
//            }
//        }
////        System.err.println("max pro: " + maxProb);
//
//        if (maxProb > 1e-55) {
//            birdsHasShootArr.get(currentRound)[birdNum] = false;
//            return new Action(birdNum, movement);
//        } else {
//            return cDontShoot;
//        }
        return cDontShoot;

//        int[] lGuess = this.guess(pState, pDue);
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

    }

    /**
     * Guess the species!
     * This function will be called at the end of each round, to give you
     * a chance to identify the species of the birds for extra points.
     * <p>
     * Fill the vector with guesses for the all birds.
     * Use SPECIES_UNKNOWN to avoid guessing.
     *
     * @param pState the GameState object with observations etc
     * @param pDue   time before which we must have returned
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
     * @param pBird  the bird you hit
     * @param pDue   time before which we must have returned
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