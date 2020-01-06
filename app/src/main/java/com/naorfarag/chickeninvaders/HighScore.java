package com.naorfarag.chickeninvaders;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

import hari.bounceview.BounceView;


public class HighScore extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private ArrayList<PlayerAttributes> gameScores = new ArrayList<>();
    private DatabaseReference dbref;
    private Finals.HOF_RANKS[] hofRanks;
    private TextView[] textViews = new TextView[Finals.HIGH_SCORE_COUNT];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_score);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        BounceView.addAnimTo(findViewById(R.id.backButton));

        hofRanks = Finals.HOF_RANKS.values();
        dbref = FirebaseDatabase.getInstance().getReference();

        for (int i = 0; i < Finals.HIGH_SCORE_COUNT; i++) {
            // Initialize all textViews - textView00, textView01 ..
            textViews[i] = findViewById(getResources().getIdentifier("textView0" + i, "id", getPackageName()));
            textViews[i].setText(hofRanks[i].getRank());
            BounceView.addAnimTo(textViews[i]);
        }

        updateDataBase();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        Objects.requireNonNull(mapFragment).getMapAsync(this);
    }

    public void updateDataBase() {
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                double longitude;
                double latitude;
                int score;
                String nickname = "";
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    try {
                        nickname = ds.child(Finals.NICKNAME).getValue(String.class);
                        score = ds.child(Finals.SCORE).getValue(Integer.class);
                        longitude = ds.child(Finals.LONGITUDE).getValue(Double.class);
                        latitude = ds.child(Finals.LATITUDE).getValue(Double.class);
                    } catch (Exception e) {
                        longitude = 0;
                        latitude = 0;
                        score = 0;
                    }
                    gameScores.add(new PlayerAttributes(score, nickname, longitude, latitude));
                }

                for (int i = 0; i < Finals.HIGH_SCORE_COUNT; i++) {
                    textViews[i].setText(hofRanks[i].getRank() + gameScores.get(i).getScore() + ", " + gameScores.get(i).getNickname());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        dbref.addListenerForSingleValueEvent(valueEventListener);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(Finals.UI_FLAGS);
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
        map.clear();
        map.addMarker(new MarkerOptions().position(playerClicked).title("Your location"));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(playerClicked, 18.0f));
    }

    public LatLng getLatLng(int index) {
        double longitude = 0;
        double latitude = 0;
        try {
            longitude = gameScores.get(index).getLongitude();
            latitude = gameScores.get(index).getLatitude();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("GPS", "Long " + longitude + "Lat " + latitude);
        }
        return new LatLng(latitude, longitude);
    }
}