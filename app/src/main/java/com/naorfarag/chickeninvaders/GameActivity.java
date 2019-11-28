package com.naorfarag.chickeninvaders;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


public class GameActivity extends AppCompatActivity {

    private ChickenInvadersView chickenInvadersView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Hide navigation bar & set full screen layout & lock portrait mode
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Get username
        String nickname = "";
        Bundle bundle = getIntent().getExtras();
        if (bundle != null)
            nickname = bundle.getString("nickname");
        // Set the main play game drawer view
        chickenInvadersView = new ChickenInvadersView(this, nickname);
        setContentView(chickenInvadersView);
    }


    // Pausing the game when activity is paused
    @Override
    protected void onPause() {
        super.onPause();
        chickenInvadersView.pause();
    }

    // Running the game when activity is resumed
    @Override
    protected void onResume() {
        super.onResume();
        chickenInvadersView.resume();
    }

    // Set window layout
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

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        chickenInvadersView.playing = false;
        builder.setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ChickenInvadersView.stopMusic();
                        chickenInvadersView.pause();
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        finish();
                        startActivity(intent);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        chickenInvadersView.resume();
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}

