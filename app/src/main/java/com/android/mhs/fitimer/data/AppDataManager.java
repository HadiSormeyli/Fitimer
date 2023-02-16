package com.android.mhs.fitimer.data;

import com.android.mhs.fitimer.data.local.db.DbHelper;
import com.android.mhs.fitimer.data.local.prefs.PreferencesHelper;
import com.android.mhs.fitimer.model.Timer;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Flowable;


public class AppDataManager implements DataManager {

    private final DbHelper dbHelper;
    private final PreferencesHelper preferencesHelper;

    @Inject
    public AppDataManager(
            DbHelper dbHelper
            , PreferencesHelper preferencesHelper
    ) {
        this.dbHelper = dbHelper;
        this.preferencesHelper = preferencesHelper;
    }

    @Override
    public boolean isNight() {
        return preferencesHelper.isNight();
    }

    @Override
    public void setNight(boolean isNight) {
        preferencesHelper.setNight(isNight);
    }

    @Override
    public String getLanguage() {
        return preferencesHelper.getLanguage();
    }

    @Override
    public void setLanguage(String lan) {
        preferencesHelper.setLanguage(lan);
    }

    @Override
    public boolean guideIsShowed(String guide) {
        return preferencesHelper.guideIsShowed(guide);
    }

    @Override
    public boolean isShowUpdateWarning() {
        return preferencesHelper.isShowUpdateWarning();
    }

    @Override
    public void showUpdateWarning(boolean show) {
        preferencesHelper.showUpdateWarning(show);
    }

    @Override
    public Completable insertTimer(Timer timer) {
        return dbHelper.insertTimer(timer);
    }

    @Override
    public Completable deleteTimer(Timer timer) {
        return dbHelper.deleteTimer(timer);
    }

    @Override
    public Completable updateTimer(Timer timer) {
        return dbHelper.updateTimer(timer);
    }

    @Override
    public Flowable<List<Timer>> getAllTimers() {
        return dbHelper.getAllTimers();
    }
}
