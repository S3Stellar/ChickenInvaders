package com.naorfarag.chickeninvaders;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


public class LoginActivity extends AppCompatActivity {

    private MediaPlayer loginSound;

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
        if (view == findViewById(R.id.startButton)) {
            // Send nickname to game manager
            Intent intent = new Intent(getApplicationContext(), GameActivity.class);
            intent.putExtra("nickname", ((EditText) findViewById(R.id.nickText)).getText().toString());

            // Pop game play keys
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Click left side of the screen to move left\n" +
                            "Click right side of the screen to move right", Toast.LENGTH_LONG).show();
                }
            }, 0);

            // Start the game
            startActivity(intent);
        }
        // Show score ranking
        if (view == findViewById(R.id.hallOfFame))
            startActivity(new Intent(this, HighScore.class));
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
        builder.setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        stopMusic();
                        Intent startMain = new Intent(Intent.ACTION_MAIN);
                        startMain.addCategory(Intent.CATEGORY_HOME);
                        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(startMain);
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
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

    @Override
    public void onDestroy() {
        stopMusic();
        super.onDestroy();
    }
}
