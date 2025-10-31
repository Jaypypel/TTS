package com.example.neptune.ttsapp.Network;

import android.content.Context;

import com.example.neptune.ttsapp.AppExecutors;
import com.example.neptune.ttsapp.SessionManager;
import com.example.neptune.ttsapp.repository.QueryRepository;
import com.example.neptune.ttsapp.repository.TaskRepository;
import com.example.neptune.ttsapp.repository.UserRepository;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
@InstallIn(SingletonComponent.class)
public class NetworkConfig {

    private static final String BASE_URL = "http://192.168.1.13:8080";


//    @Provides
//    @Singleton
//    public BasicAuthInterceptor provideBasicAuthInterceptor() {
//        return new BasicAuthInterceptor();
//    }

    @Provides
    @Singleton
    public static SessionManager provideSessionManager(@ApplicationContext Context appContext) {
        return new SessionManager(appContext);
    }

    @Provides
    @Singleton
    public BasicAuthInterceptor provideBasicAuthInterceptor(SessionManager sessionManager) {
        return new BasicAuthInterceptor(sessionManager);
    }
    @Provides
    @Singleton
    public static OkHttpClient provideOkHttpClient(BasicAuthInterceptor authInterceptor){
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return new OkHttpClient.Builder()
                .addInterceptor(authInterceptor).addInterceptor(loggingInterceptor)
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
                .client(okHttpClient)
                .build();
    }


    /**
     * @param retrofit
     * @return
     */
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


    @Provides
    @Singleton
    public static RoleServiceInterface provideRoleService(Retrofit retrofit){
        return retrofit.create(RoleServiceInterface.class);
    }


    @Provides
    @Singleton
    public static QueryServiceInterface provideQueryService(Retrofit retrofit){
        return retrofit.create(QueryServiceInterface.class);
    }


    @Provides
    @Singleton
    public static UserRepository provideUserRepository(
            UserServiceInterface userService,
            AppExecutors appExecutors
    ) {
        return new UserRepository(userService, appExecutors);
    }


    @Provides
    @Singleton
    public static TaskRepository provideTaskRepository(
            TaskHandlerInterface taskHandlerInterface,
            AppExecutors appExecutors
    ) {
        return new TaskRepository(taskHandlerInterface, appExecutors);
    }

    @Provides
    @Singleton
    public static QueryRepository provideQueryRepository(
            QueryServiceInterface queryService
    ) {
        return new QueryRepository(queryService);
    }


//    @Provides
//    @Singleton
//    UserServiceInterface provid
}
