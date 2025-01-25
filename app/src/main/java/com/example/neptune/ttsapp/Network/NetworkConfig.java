package com.example.neptune.ttsapp.Network;

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

    private static final String BASE_URL = "http://192.168.0.100:8080";

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
    public static Retrofit provideRetroFit(OkHttpClient okHttpClient){
        return new Retrofit
                .Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient).
                build();
    }



    @Provides
    @Singleton
    public static UserServiceInterface provideService(Retrofit retrofit){
        return retrofit.create(UserServiceInterface.class);
    }

    @Provides
    @Singleton
    public static MeasurableServiceInterface provideMeasurableService(Retrofit retrofit){
        return retrofit.create(MeasurableServiceInterface.class);
    }

    @Provides
    @Singleton
    public static ProjectServiceInterface provideProejectService(Retrofit retrofit){
        return retrofit.create(ProjectServiceInterface.class);
    }

    @Provides
    @Singleton
    public static DailyTimeShareInterface provideDTSService(Retrofit retrofit){
        return retrofit.create(DailyTimeShareInterface.class);
    }
    @Provides
    @Singleton
    public static TaskServiceInterface provideTask0Service(Retrofit retrofit){
        return retrofit.create(TaskServiceInterface.class);
    }
    @Provides
    @Singleton
    public static ActivityServiceInterface provideActivityService(Retrofit retrofit){
        return retrofit.create(ActivityServiceInterface.class);
    }

    @Provides
    @Singleton
    public static DTSMeasurableInterface provideDTSMeasurableService(Retrofit retrofit){
        return retrofit.create(DTSMeasurableInterface.class);
    }

    @Provides
    @Singleton
    public static TaskHandlerInterface provideTaskHandlerService(Retrofit retrofit){
        return retrofit.create(TaskHandlerInterface.class);
    }

    @Provides
    @Singleton
    public static TimeShareServiceInterface provideTimeShareService(Retrofit retrofit){
        return retrofit.create(TimeShareServiceInterface.class);
    }

    @Provides
    @Singleton
    public static ReportServiceInterface provideReportService(Retrofit retrofit){
        return retrofit.create(ReportServiceInterface.class);
    }




//    @Provides
//    @Singleton
//    UserServiceInterface provid
}
