package com.android.mhs.fitimer.di;

import android.app.Application;
import android.content.Context;

import com.android.mhs.fitimer.data.AppDataManager;
import com.android.mhs.fitimer.data.DataManager;
import com.android.mhs.fitimer.data.local.prefs.AppPreferencesHelper;
import com.android.mhs.fitimer.data.local.prefs.PreferencesHelper;
import com.android.mhs.fitimer.utils.rx.AppSchedulerProvider;
import com.android.mhs.fitimer.utils.rx.SchedulerProvider;

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
    Context provideContext(Application application) {
        return application;
    }

    @Provides
    @Singleton
    DataManager provideDataManager(AppDataManager appDataManager) {
        return appDataManager;
    }

    @Provides
    @Singleton
    PreferencesHelper providePreferencesHelper(AppPreferencesHelper appPreferencesHelper) {
        return appPreferencesHelper;
    }

    @Provides
    SchedulerProvider provideSchedulerProvider() {
        return new AppSchedulerProvider();
    }
}
