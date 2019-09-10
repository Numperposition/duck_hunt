import java.util.ArrayList;

class Player {
    private static final double threshold = 0.0;

    private HMMmodel[] hmms;

    public Player() {
        hmms = new HMMmodel[6];
        for(int i = 0; i < 6; i++)
        {
            HMMmodel model = new HMMmodel();
            hmms[i] = model;
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
       // System.out.println("get Num new Turn = "+ pState.getNumNewTurns());
        //shoot after 100-bird_num t.
        if(pState.getNumNewTurns() > 100 - pState.getNumBirds())
        {
            int[] guessBirds = guess(pState, pDue);
            for(int i = 0; i < pState.getNumBirds(); i++)
            {
                Bird bird = pState.getBird(i);
//                if(guessBirds[i] == Constants.SPECIES_BLACK_STORK || bird.isDead())
//                    return cDontShoot;
                if(bird.isAlive() && guessBirds[i] != Constants.SPECIES_BLACK_STORK)
                {
                    double maxProb = 0.0;
                    int movement = Constants.MOVE_DEAD;
                    for(int j = 0; j < Constants.COUNT_MOVE; j++)
                    {
                        int[] obserSeq = hmms[guessBirds[i]].getObserSeq(bird, j);
                        double prob = hmms[guessBirds[i]].calculateProb(obserSeq);
                        if(prob > maxProb)
                        {
                            maxProb = prob;
                            movement = j;
                        }
                    }
                    if(movement != Constants.MOVE_DEAD)
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
        if(pState.getRound() == 0)
        {
            for(int i = 0; i < pState.getNumBirds(); i++)
            {
                lGuess[i] = Constants.SPECIES_PIGEON;
            }
        }
        else
        {
            for (int i = 0; i < pState.getNumBirds(); ++i)
            {
                Bird bird = pState.getBird(i);
                double maxProb = 0.0;
                int species = Constants.SPECIES_UNKNOWN;
                for(int j = 0; j < 6; j++)
                {
                    double prob = hmms[j].calculateProb(hmms[j].getObserSeq(bird));
                    if(prob > maxProb)
                    {
                        maxProb = prob;
                        species = j;
                    }

                }
                if(maxProb > threshold)
                    lGuess[i] = species;
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
        for(int i = 0; i < pSpecies.length; i++)
        {
            Bird bird = pState.getBird(i);
            int species = pSpecies[i];
            hmms[species].trainModel(bird);
        }

    }



    public static final Action cDontShoot = new Action(-1, -1);
}
