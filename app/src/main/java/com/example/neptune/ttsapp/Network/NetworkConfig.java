package com.example.neptune.ttsapp.Network;

import androidx.core.view.WindowInsetsCompat;

import com.example.neptune.ttsapp.User;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
@InstallIn(SingletonComponent.class)
public class NetworkConfig {
    @Provides
    @Singleton
    public static UserServiceInterface provideService(){
        return new Retrofit.Builder().baseUrl("http://localhost:8080")
                .addConverterFactory(GsonConverterFactory.create()).
                build().create(UserServiceInterface.class);
    }

//    @Provides
//    @Singleton
//    UserServiceInterface provid
}
