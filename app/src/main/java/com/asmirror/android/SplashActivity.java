package com.asmirror.android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = getSharedPreferences("Settings", Context.MODE_PRIVATE);

        // checking preferences
        if (preferences.getBoolean("FirstTimeLogin", false)) {
            SendUserToMainActivity();
        } else {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("FirstTimeLogin", true);
            editor.apply();
            setContentView(R.layout.activity_splash);
            Button GetStartedButton = findViewById(R.id.splash_button_get_started);
            GetStartedButton.setOnClickListener(v -> SendUserToMainActivity());
        }
    }

    private void SendUserToMainActivity() {
        Intent MainActivity = new Intent(this, MainActivity.class);
        startActivity(MainActivity);
        finish();
    }
}