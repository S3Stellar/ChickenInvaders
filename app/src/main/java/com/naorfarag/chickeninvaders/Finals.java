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

    // New shooting mechanics constants
    public static final int BULLET_SPEED = 2000;
    public static final int MAX_BULLETS = 10;
    public static final int BULLET_DAMAGE = 1;
    public static final int ENEMY_HEALTH = 1;
    public static final int BOSS_HEALTH = 10;
    
    // Power-up constants
    public static final int RAPID_FIRE_DURATION = 5000; // 5 seconds
    public static final int SHIELD_DURATION = 3000; // 3 seconds
    public static final int DOUBLE_SCORE_DURATION = 10000; // 10 seconds
    public static final int SPREAD_SHOT_DURATION = 8000; // 8 seconds
    
    // Performance constants
    public static final int MAX_PARTICLES = 50;
    public static final int PARTICLE_LIFE = 1000; // 1 second
    
    // Difficulty progression
    public static final int BOSS_SPAWN_SCORE = 100;
    public static final int DIFFICULTY_INCREASE_INTERVAL = 50;
    
    // Combo system
    public static final int COMBO_MULTIPLIER_MAX = 5;
    public static final int COMBO_TIMEOUT = 3000; // 3 seconds
    
    // Achievement constants
    public static final int ACHIEVEMENT_FIRST_KILL = 1;
    public static final int ACHIEVEMENT_COMBO_5 = 5;
    public static final int ACHIEVEMENT_SCORE_100 = 100;
    public static final int ACHIEVEMENT_BOSS_DEFEAT = 1;

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
    public static final String USER = "user";

    public static final String PLAY_AGAIN = "Click to play again!";
    public static final String ARIAL_FONT = "Arial";

    public static final String INST_MSG1 = "Click left side of the screen to move left\n";
    public static final String INST_MSG2 = "Click right side of the screen to move right";
    public static final String INST_MSG3 = "Tap anywhere to shoot!";
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
    
    public enum PowerUpType {
        RAPID_FIRE, SHIELD, DOUBLE_SCORE, SPREAD_SHOT, EXTRA_LIFE
    }
    
    public enum WeaponType {
        NORMAL, RAPID, SPREAD, LASER
    }
}
