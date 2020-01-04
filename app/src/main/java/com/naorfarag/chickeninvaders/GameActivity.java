package com.naorfarag.chickeninvaders;

import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.github.nisrulz.sensey.Sensey;


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
        int lanes = Finals.DEFAULT_LANES;
        boolean isTilt = false;
        double latitude = 0;
        double longitude = 0;

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            nickname = bundle.getString(Finals.NICKNAME);
            lanes = bundle.getInt(Finals.LANES);
            isTilt = bundle.getBoolean(Finals.IS_TILT);
            latitude = bundle.getDouble(Finals.LATITUDE);
            longitude = bundle.getDouble(Finals.LONGITUDE);
        }

        // Set the main play game drawer view
        chickenInvadersView = new ChickenInvadersView(this, nickname, lanes, isTilt, latitude, longitude);
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

    @Override
    public void onStop() {
        super.onStop();
        chickenInvadersView.pause();
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
        builder.setMessage(Finals.EXIT_CHECK_MSG)
                .setCancelable(false)
                .setPositiveButton(Finals.YES, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ChickenInvadersView.stopMusic();
                        chickenInvadersView.pause();
                        finish();
                    }
                })
                .setNegativeButton(Finals.NO, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        chickenInvadersView.resume();
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Sensey.getInstance().stop();
    }
}

