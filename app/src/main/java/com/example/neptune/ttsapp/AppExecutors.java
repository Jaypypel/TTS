package com.example.neptune.ttsapp;

import com.example.neptune.ttsapp.Network.MainThreadExecutor;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AppExecutors {
    private final Executor diskIO;
    private final Executor networkIO;
    private final Executor mainThread;


    @Inject
    public AppExecutors(){
        this(Executors.newSingleThreadExecutor(),Executors.newFixedThreadPool(3),new MainThreadExecutor());
    }

    public AppExecutors(Executor diskIO, Executor networkIO, Executor mainThread) {
        this.diskIO = diskIO;
        this.networkIO = networkIO;
        this.mainThread = mainThread;
    }

    public Executor getDiskIO() {
        return diskIO;
    }

    public Executor getNetworkIO() {
        return networkIO;
    }

    public Executor getMainThread() {
        return mainThread;
    }
}
