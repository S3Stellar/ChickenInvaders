package com.naorfarag.chickeninvaders;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import hari.bounceview.BounceView;


public class LoginActivity extends AppCompatActivity implements SettingsDialog.DialogListener {

    private MediaPlayer loginSound;

    private int lanesAmount = Finals.DEFAULT_LANES;
    private boolean isTilt = false;

    private FusedLocationProviderClient mFusedLocationClient;
    private double latitude;
    private double longitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();

        loginSound = MediaPlayer.create(getApplicationContext(), R.raw.gameon);
        loginSound.setLooping(true);
        loginSound.start();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        BounceView.addAnimTo(findViewById(R.id.settingsIcon));
        BounceView.addAnimTo(findViewById(R.id.startButton));
        BounceView.addAnimTo(findViewById(R.id.hallOfFame));
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
            intent.putExtra(Finals.LATITUDE, latitude);
            intent.putExtra(Finals.LONGITUDE, longitude);

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
            getWindow().getDecorView().setSystemUiVisibility(Finals.UI_FLAGS);
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

    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.getLastLocation().addOnCompleteListener(
                        new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                Location location = task.getResult();
                                if (location == null || location.getLatitude() == 0 || location.getLongitude() == 0) {
                                    requestNewLocationData();
                                } else {
                                    latitude = location.getLatitude();
                                    longitude = location.getLongitude();
                                }
                            }
                        }
                );
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            requestPermissions();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        );

    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();
        }
    };

    private void requestPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                Finals.PERMISSION_ID
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Finals.PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
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
        if (checkPermissions()) {
            getLastLocation();
        }
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
