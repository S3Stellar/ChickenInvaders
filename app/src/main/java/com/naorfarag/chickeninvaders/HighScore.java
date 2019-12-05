package com.naorfarag.chickeninvaders;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


public class HighScore extends AppCompatActivity implements Finals {


    private TextView[] textViews = new TextView[HIGH_SCORE_COUNT];
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_score);

        HOF_RANKS[] hof_ranks = HOF_RANKS.values();
        sharedPreferences = getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        for (int i = 0; i < HIGH_SCORE_COUNT; i++) {
            textViews[i] = findViewById(getResources().getIdentifier("textView0" + i, "id", getPackageName()));
            textViews[i].setText(hof_ranks[i].getRank() + sharedPreferences.getInt(SCORE + i, 0) + ", " + sharedPreferences.getString(NICKNAME + i, ""));
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
}
