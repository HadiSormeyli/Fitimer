package com.android.mhs.fitimer.di;

import android.content.Context;

import androidx.room.Room;

import com.android.mhs.fitimer.BuildConfig;
import com.android.mhs.fitimer.data.local.db.AppDataBase;
import com.android.mhs.fitimer.data.local.db.AppDbHelper;
import com.android.mhs.fitimer.data.local.db.DbHelper;
import com.android.mhs.fitimer.data.local.db.dao.TimerDao;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {

    @Provides
    @Singleton
    AppDataBase provideAppDataBase(Context context) {
        return Room.databaseBuilder(
                        context
                        , AppDataBase.class
                        , BuildConfig.DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .build();
    }

    @Provides
    @Singleton
    DbHelper provideDbHelper(AppDbHelper appDbHelper) {
        return appDbHelper;
    }

    @Provides
    @Singleton
    TimerDao provideTimerDao(AppDataBase appDataBase) {
        return appDataBase.timerDao();
    }
}
