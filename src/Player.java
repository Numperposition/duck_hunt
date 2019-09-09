import java.util.ArrayList;

class Player {

    // the array stores each bird's observation list
    private ArrayList<ArrayList<Integer>> observationOfAll = new ArrayList<>();
    private Boolean[] birdsAlive;
    private ArrayList<HMM> hmms = new ArrayList<>();

    public Player() {
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
        System.out.println("get Num new Turn = "+ pState.getNumNewTurns());

        int birdAmount = pState.getNumBirds();

        // if it is a new turn
        if (pState.getNumNewTurns() == 1) {
            birdsAlive = new Boolean[birdAmount];
            observationOfAll = new ArrayList<>();
            hmms = new ArrayList<>();
            for (int i = 0; i < birdAmount; i++) {
                birdsAlive[i] = true; // all alive
                observationOfAll.add(new ArrayList<>());
            }
        }

        for (int i = 0; i < birdAmount; i++) {
            int observation = pState.getBird(i).getLastObservation();
            if (observation == -1) {
                birdsAlive[i] = false; // bird i died
            } else {
                observationOfAll.get(i).add(observation);
            }
        }

        if (pState.getNumNewTurns() >= 51) {
            if (pState.getNumNewTurns() == 51) {
                // train the models
                for (int i = 0; i < birdAmount; i++) {
                    HMM hmm = new HMM(observationOfAll.get(i));
                    hmm.modelTrain();
                    hmms.add(hmm);
                }
            }

            for (int i = 0; i < birdAmount; i++) {
                if (birdsAlive[i]) {
                    HMM hmm = hmms.get(i);
                    int movement = hmm.getMostLikelyObservation(observationOfAll.get(i));
                    // predict that bird i's movement and shoot at it.
                    return new Action(i, movement);
                }
            }
        }

        // choose not to shoot.
        return cDontShoot;
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
        for (int i = 0; i < pState.getNumBirds(); ++i)
            lGuess[i] = Constants.SPECIES_UNKNOWN;
        return lGuess;
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
     * @param pState the GameState object with observations etc
     * @param pSpecies the vector with species
     * @param pDue time before which we must have returned
     */
    public void reveal(GameState pState, int[] pSpecies, Deadline pDue) {
    }

    public static final Action cDontShoot = new Action(-1, -1);
}
