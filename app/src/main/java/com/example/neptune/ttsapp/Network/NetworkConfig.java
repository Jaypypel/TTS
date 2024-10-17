package com.example.neptune.ttsapp.Network;

import androidx.core.view.WindowInsetsCompat;

import com.example.neptune.ttsapp.DTO.DailyTimeShareDTO;
import com.example.neptune.ttsapp.User;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
@InstallIn(SingletonComponent.class)
public class NetworkConfig {

    @Provides
    @Singleton
    public static OkHttpClient provideOkHttpClient(){
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)  // Increase connection timeout
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)     // Increase read timeout
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)    // Increase write timeout
                .build();
    }

    @Provides
    @Singleton
    public static UserServiceInterface provideService(){
        return new Retrofit.Builder().baseUrl("http://192.168.0.108:8080")
                .addConverterFactory(GsonConverterFactory.create()).client(provideOkHttpClient()).
                build().create(UserServiceInterface.class);
    }


    @Provides
    @Singleton
    public static MeasurableServiceInterface provideMeasurableService(){
        return new Retrofit.Builder().baseUrl("http://192.168.0.108:8080")
                .addConverterFactory(GsonConverterFactory.create()).client(provideOkHttpClient()).
                build().create(MeasurableServiceInterface.class);
    }
    @Provides
    @Singleton
    public static ProjectServiceInterface provideProejectService(){
        return new Retrofit.Builder().baseUrl("http://192.168.0.108:8080")
                .addConverterFactory(GsonConverterFactory.create()).client(provideOkHttpClient()).
                build().create(ProjectServiceInterface.class);
    }

    @Provides
    @Singleton
    public static DailyTimeShareInterface provideDTSService(){
        return new Retrofit.Builder().baseUrl("http://192.168.0.108:8080")
                .addConverterFactory(GsonConverterFactory.create()).client(provideOkHttpClient()).
                build().create(DailyTimeShareInterface.class);
    }
    @Provides
    @Singleton
    public static TaskServiceInterface provideTask0Service(){
        return new Retrofit.Builder().baseUrl("http://192.168.0.108:8080")
                .addConverterFactory(GsonConverterFactory.create()).client(provideOkHttpClient()).
                build().create(TaskServiceInterface.class);
    }




//    @Provides
//    @Singleton
//    UserServiceInterface provid
}
