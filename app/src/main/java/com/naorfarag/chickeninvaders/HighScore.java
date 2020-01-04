package com.naorfarag.chickeninvaders;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class HighScore extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private SharedPreferences sharedPreferences;
    private TextView[] textViews = new TextView[Finals.HIGH_SCORE_COUNT];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_score);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Finals.HOF_RANKS[] hofRanks = Finals.HOF_RANKS.values();
        sharedPreferences = getSharedPreferences(Finals.SHARED_PREF, Context.MODE_PRIVATE);
        for (int i = 0; i < Finals.HIGH_SCORE_COUNT; i++) {
            // Initialize all textViews - textView00, textView01 ..
            textViews[i] = findViewById(getResources().getIdentifier("textView0" + i, "id", getPackageName()));

            textViews[i].setText(hofRanks[i].getRank() + sharedPreferences.getInt
                    (Finals.SCORE + i, 0) + ", " + sharedPreferences.getString
                    (Finals.NICKNAME + i, ""));
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        LatLng champ = getLatLng(0);
        map.addMarker(new MarkerOptions().position(champ).title("Your location"));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(champ, 18.0f));
    }

    public void onHighScoreClicked(View view) {
        String name = view.getResources().getResourceName(view.getId());
        String substring = name.length() > 2 ? name.substring(name.length() - 2) : name;
        int num = Integer.parseInt(substring);

        LatLng playerClicked = getLatLng(num);
        map.addMarker(new MarkerOptions().position(playerClicked).title("Your location"));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(playerClicked, 18.0f));
    }

    public LatLng getLatLng(int index){
        double longitude = 0;
        double latitude = 0;
        try {
            longitude = Double.parseDouble(sharedPreferences.getString(Finals.LONGITUDE + index, ""));
            latitude = Double.parseDouble(sharedPreferences.getString(Finals.LATITUDE + index, ""));
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("GPS", "Long " + longitude + "Lat " + latitude);
        }
        return new LatLng(latitude, longitude);
    }
}