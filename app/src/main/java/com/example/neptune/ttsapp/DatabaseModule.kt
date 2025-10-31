//package com.example.neptune.ttsapp
//
//import android.content.Context
//import androidx.room.Room
//import com.example.neptune.ttsapp.displayTaskToLearners.AppDatabase
//import com.example.neptune.ttsapp.displayTaskToLearners.TaskDao
//import com.example.neptune.ttsapp.paging.RemoteKeysDao
//import dagger.Module
//import dagger.Provides
//import dagger.hilt.InstallIn
//import dagger.hilt.android.qualifiers.ApplicationContext
//import dagger.hilt.components.SingletonComponent
//import javax.inject.Singleton
//
//@Module
//@InstallIn(SingletonComponent::class)
//object DatabaseModule {
//
//    @Provides
//    @Singleton
//    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
//        return Room.databaseBuilder(
//            context.applicationContext,
//            AppDatabase::class.java,
//            "tts_database"
//        ).fallbackToDestructiveMigration().build() // Added fallbackToDestructiveMigration
//    }
//
//    @Provides
//    @Singleton
//    fun provideTaskDao(appDatabase: AppDatabase): TaskDao {
//        return appDatabase.taskDao()
//    }
//
//    @Provides
//    @Singleton
//    fun provideRemoteKeysDao(appDatabase: AppDatabase): RemoteKeysDao {
//        return appDatabase.remoteKeysDao()
//    }
//}
