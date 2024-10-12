package com.example.neptune.ttsapp;

import android.app.Application;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class TTSApplication extends Application {

    private static TTSApplication mInstance;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
    }

    public static synchronized TTSApplication getInstance() {
        return mInstance;
    }

    public void setConnectivityListener(InternetConnectivity.ConnectivityReceiverListener listener) {
        InternetConnectivity.connectivityReceiverListener = listener;
    }
}
