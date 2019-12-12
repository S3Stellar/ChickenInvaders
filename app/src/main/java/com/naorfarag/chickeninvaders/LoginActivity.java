package com.naorfarag.chickeninvaders;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


public class LoginActivity extends AppCompatActivity {

    private MediaPlayer loginSound;
    private SeekBar seekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        seekBar = new SeekBar(getApplicationContext());
        seekBar.setProgress(4);
        loginSound = MediaPlayer.create(getApplicationContext(), R.raw.gameon);
        loginSound.setLooping(true);
        loginSound.start();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
    }

    public void onButton(View view) {

        // Settings Icon Clicked
        if (view == findViewById(R.id.settingsIcon)) {
            chooseLanes();
        }
        // Start button Clicked
        if (view == findViewById(R.id.startButton)) {
            // Send nickname to game manager
            Intent intent = new Intent(getApplicationContext(), GameActivity.class);
            intent.putExtra("nickname", ((EditText) findViewById(R.id.nickText)).getText().toString());
            intent.putExtra("lanes", seekBar.getProgress());

            // Pop game play keys
            Toast.makeText(getApplicationContext(), "Click left side of the screen to move left\n" +
                    "Click right side of the screen to move right", Toast.LENGTH_LONG).show();

            // Start the game
            loginSound.seekTo(0);
            startActivity(intent);
        }

        // Hall Of Fame button Clicked
        if (view == findViewById(R.id.hallOfFame))
            startActivity(new Intent(this, HighScore.class));
    }

    public void chooseLanes() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Lane's amount : ");
        seekBar.setMax(8);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            seekBar.setMin(3);
        }
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        seekBar.setLayoutParams(lp);
        builder.setView(seekBar);
        builder.setCancelable(false);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(getApplicationContext(), "Chicken's amount " + seekBar.getProgress(), Toast.LENGTH_SHORT).show();
            }
        });
        if (seekBar.getParent() != null) {
            ((ViewGroup) seekBar.getParent()).removeView(seekBar); // <- fix reenter dialog few times
        }
        builder.show();
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

    // Hide keyboard when clicked on screen
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = this.getCurrentFocus();
        if (imm != null) {
            imm.hideSoftInputFromWindow(view != null ? view.getWindowToken() : null,0);
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onDestroy() {
        stopMusic();
        super.onDestroy();
    }
}
