package com.naorfarag.chickeninvaders;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;



public class LoginActivity extends AppCompatActivity {

    private EditText userNickname;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.hide();

        //set up full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
    }

    public void onStart(View view){
        Intent intent = new Intent(getApplicationContext(), GameActivity.class);
        userNickname = findViewById(R.id.nickText);
        intent.putExtra("nickname",userNickname.getText().toString());
        startActivity(intent);
    }
}
