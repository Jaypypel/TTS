package com.example.neptune.ttsapp;

import com.example.neptune.ttsapp.Network.MainThreadExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {
    @Provides
    @Singleton
    public Executor provideDiskIOExecutor() {
        return Executors.newSingleThreadExecutor();
    }

    @Provides
    @Singleton
    public Executor provideNetworkIOExecutor() {
        return Executors.newFixedThreadPool(3);
    }

    @Provides
    @Singleton
    public Executor provideMainThreadExecutor() {
        return  new MainThreadExecutor();
    }
}
