package com.naorfarag.chickeninvaders;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


public class HighScore extends AppCompatActivity {

    private TextView[] textViews = new TextView[Finals.HIGH_SCORE_COUNT];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_score);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Finals.HOF_RANKS[] hofRanks = Finals.HOF_RANKS.values();
        SharedPreferences sharedPreferences = getSharedPreferences(Finals.SHARED_PREF, Context.MODE_PRIVATE);
        for (int i = 0; i < Finals.HIGH_SCORE_COUNT; i++) {
            // Initialize all textViews - textView00, textView01 ..
            textViews[i] = findViewById(getResources().getIdentifier("textView0" + i, "id", getPackageName()));
            textViews[i].setText(hofRanks[i].getRank() + sharedPreferences.getInt
                    (Finals.SCORE + i, 0) + ", " + sharedPreferences.getString
                    (Finals.NICKNAME + i, ""));
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    public void onBack(View view) {
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
