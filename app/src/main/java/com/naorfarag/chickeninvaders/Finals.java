package com.naorfarag.chickeninvaders;

import android.view.View;

public final class Finals {

    public static final int MAX_LIFE = 3;
    public static final int MAX_SCORE = 9999;
    public static final int STARS_COUNT = 100;
    public static final int HIGH_SCORE_COUNT = 4;
    public static final int EGG_BONUS = 10;
    public static final int SHIP_SPEED = 1000;

    public static final int OUT_OF_BOUNDS = -650;
    public static final int MAX_VOLUME = 1;

    public static final int DEFAULT_LANES = 4;
    public static final int MAX_LANES = 8;
    public static final int MIN_LANES = 3;

    public static final int PERMISSION_ID = 44;

    public static final int UI_FLAGS =
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;

    public static final String LATITUDE = "LATITUDE";
    public static final String LONGITUDE = "LONGITUDE";

    public static final String SHARED_PREF = "SHARED_PREF_NAME";
    public static final String LANES = "lanes";
    public static final String SCORE = "score";
    public static final String IS_TILT = "isTilt";
    public static final String NICKNAME = "nickname";

    public static final String PLAY_AGAIN = "Click to play again!";
    public static final String ARIAL_FONT = "Arial";

    public static final String INST_MSG1 = "Click left side of the screen to move left\n";
    public static final String INST_MSG2 = "Click right side of the screen to move right";
    public static final String EXIT_CHECK_MSG = "Are you sure you want to exit?";
    public static final String YES = "Yes";
    public static final String NO = "No";
    public static final String INSANE = "Insane";
    public static final String EASY = "Easy";
    public static final String OK = "Ok";


    public enum HOF_RANKS {
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
