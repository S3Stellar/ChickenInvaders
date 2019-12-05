package com.naorfarag.chickeninvaders;

interface Finals {

    int MAX_LIFE = 3;
    int MAX_SCORE = 9999;
    int ENEMIES_COUNT = 3;
    int STARS_COUNT = 100;
    int HIGH_SCORE_COUNT = 4;


    String SHARED_PREF = "SHARED_PREF_NAME";
    String SCORE = "score";
    String NICKNAME = "nickname";

    String PLAY_AGAIN = "Click to play again!";
    String ARIAL_FONT = "Arial";

    int OUT_OF_BOUNDS = -650;

    enum HOF_RANKS {
        FIRST("First : "), SECOND("Second : "), THIRD("Third : "), FOURTH("Fourth : ");
        private String rank;

        public String getRank() {
            return this.rank;
        }

        HOF_RANKS(String rank) {
            this.rank = rank;
        }
    }
}
