package com.naorfarag.chickeninvaders;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


public class LoginActivity extends AppCompatActivity implements SettingsDialog.DialogListener {

    private MediaPlayer loginSound;
    private int lanesAmount = Finals.DEFAULT_LANES;
    private boolean isTilt = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loginSound = MediaPlayer.create(getApplicationContext(), R.raw.gameon);
        loginSound.setLooping(true);
        loginSound.start();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
    }

    public void onButton(View view) {
        // Settings Icon Clicked
        if (view == findViewById(R.id.settingsIcon)) {
            settingsDialog();
        }

        // Start button Clicked
        if (view == findViewById(R.id.startButton)) {
            // Send details to game manager
            Intent intent = new Intent(getApplicationContext(), GameActivity.class);
            intent.putExtra(Finals.NICKNAME, ((EditText) findViewById(R.id.nickText)).getText().toString());
            intent.putExtra(Finals.LANES, lanesAmount);
            intent.putExtra(Finals.IS_TILT, isTilt);

            // Pop game play keys
            Toast.makeText(getApplicationContext(), Finals.INST_MSG1 +
                    Finals.INST_MSG2, Toast.LENGTH_LONG).show();

            // Start the game
            loginSound.seekTo(0);
            startActivity(intent);
        }

        // Hall Of Fame button Clicked
        if (view == findViewById(R.id.hallOfFame))
            startActivity(new Intent(this, HighScore.class));
    }


    public void settingsDialog() {
        SettingsDialog settingsDialog = new SettingsDialog();
        settingsDialog.show(getSupportFragmentManager(), "Settings dialog");
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

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(Finals.EXIT_CHECK_MSG)
                .setCancelable(false)
                .setPositiveButton(Finals.YES, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        stopMusic();
                        Intent startMain = new Intent(Intent.ACTION_MAIN);
                        startMain.addCategory(Intent.CATEGORY_HOME);
                        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(startMain);
                        finish();
                    }
                })
                .setNegativeButton(Finals.NO, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        loginSound.start();
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (loginSound != null)
            loginSound.pause();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (loginSound != null)
            loginSound.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (loginSound != null)
            loginSound.start();
    }

    public void stopMusic() {
        if (loginSound != null) {
            loginSound.stop();
            loginSound.reset();
            loginSound.release();
            loginSound = null;
        }
    }

    // Hide keyboard when clicked on screen
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = this.getCurrentFocus();
        if (imm != null) {
            imm.hideSoftInputFromWindow(view != null ? view.getWindowToken() : null, 0);
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onDestroy() {
        stopMusic();
        super.onDestroy();
    }

    @Override
    public void applySettings(int lanesAmount, boolean isTilt) {
        this.lanesAmount = lanesAmount;
        this.isTilt = isTilt;
    }
}
