import java.util.ArrayList;

class Player {

    private static final Action cDontShoot = new Action(-1, -1);
    private ArrayList<HMM> hmms = new ArrayList<>();

    public Player() {}

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
        int birdAmount = pState.getNumBirds();
        int currentRound = pState.getRound();
        int currentTimeStep = pState.getBird(0).getSeqLength();
        int shootRound = 79;
        int[] lGuess = this.guess(pState, pDue);

        // if certain bird died, shoot ahead!
        for (int i = 0; i < birdAmount; i++) {
            if (!pState.getBird(i).isAlive()) {
                shootRound = 68;
            }
        }

        if (currentRound == 0 || currentTimeStep < shootRound) {
            return cDontShoot;
        } else {
            Double maxPatternProb = -1e11;
            int maxPatternIndex = 0;
            Double maxMoveProb = -1e10;
            int mostLikelyMove = -1;
            int targetBird = -1;

            for (int i = 0; i < birdAmount; i++) {
                Bird bird = pState.getBird(i);

                if ((bird.isAlive()) && (lGuess[i] != Constants.SPECIES_UNKNOWN)
                        && (lGuess[i] != Constants.SPECIES_BLACK_STORK)) {
                    for (int j = 0; j < hmms.size(); j++) {
                        // if guess type match one pattern,
                        // then use this pattern to predict the movement
                        if (lGuess[i] == hmms.get(j).getSpecies()) {
                            // find the most high possibility with guess type in hmms
                            Double possibility = hmms.get(j).getProb(bird, -1);
                            if (possibility > maxPatternProb) {
                                maxPatternProb = possibility;
                                maxPatternIndex = j;
                            }
                        }
                    }

                    for (int j = 0; j < Constants.COUNT_MOVE; j++) {
                        Double possibility = hmms.get(maxPatternIndex).getProb(bird, j);
                        if (possibility > maxMoveProb) {
                            mostLikelyMove = j;
                            maxMoveProb = possibility;
                            targetBird = i;
                        }
                    }
                }
            }

            if (maxMoveProb > -129.341253) {
                return new Action(targetBird, mostLikelyMove);
            } else {
                return cDontShoot;
            }
        }
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

        int birdAmount = pState.getNumBirds();
        Double maxProb = -1e10;
        int maxPatternIndex = -1;
        Double secondProb = -1e10;
        int secondPatternIndex = -1;

        int[] lGuess = new int[pState.getNumBirds()];
        for (int i = 0; i < pState.getNumBirds(); ++i)
            lGuess[i] = Constants.SPECIES_PIGEON;

        for (int i = 0; i < birdAmount; i++) {
            if (hmms.size() > 0) {
                Bird bird = pState.getBird(i);

                for (int j = 0; j < hmms.size(); j++) {
                    Double possibility = hmms.get(j).getProb(bird, -1);
                    if (possibility > maxProb) {
                        secondPatternIndex = maxPatternIndex;
                        secondProb = maxProb;

                        maxProb = possibility;
                        maxPatternIndex = j;
                    }
                }

                // if the highest 2 possibilities is the same species, then guess
                // otherwise, we choose not to guess
                if (hmms.get(maxPatternIndex).getSpecies() ==
                        hmms.get(secondPatternIndex).getSpecies())
                    lGuess[i] = hmms.get(maxPatternIndex).getSpecies();
                else
                    lGuess[i] = Constants.SPECIES_UNKNOWN;
            }
        }
        return lGuess;
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
        int birdAmount = pState.getNumBirds();
        int[] lGuess = this.guess(pState, pDue);

        for (int i = 0; i < birdAmount; i++) {
            if (lGuess[i] != Constants.SPECIES_UNKNOWN) {
                Bird bird = pState.getBird(i);
                int species = pSpecies[i];
                HMM hmm = new HMM();
                hmm.modelTrain(bird, species);
                this.hmms.add(hmm);
            }
        }
    }

}
