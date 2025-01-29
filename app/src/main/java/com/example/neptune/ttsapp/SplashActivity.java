package com.example.neptune.ttsapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIME = 1000;
    LinearProgressIndicator progressIndicator;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        progressIndicator = findViewById(R.id.progressBarSplash);
        progressIndicator.show();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SessionManager sessionManager = new SessionManager(this);
            if (sessionManager.isLoggedIn()){
                startActivity(new Intent(SplashActivity.this,TTSMainActivity.class));
            }else {
                startActivity(new Intent(SplashActivity.this,TTSLoginActivity.class));
            }
            finish();
        },SPLASH_TIME);

    }
}
