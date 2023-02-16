package com.android.mhs.fitimer.data.local.db;

import com.android.mhs.fitimer.model.Timer;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Flowable;

public class AppDbHelper implements DbHelper {

    private final AppDataBase appDataBase;

    @Inject
    public AppDbHelper(AppDataBase appDataBase) {
        this.appDataBase = appDataBase;
    }

    @Override
    public Completable insertTimer(Timer timer) {
        return appDataBase.timerDao().insert(timer);
    }

    @Override
    public Completable deleteTimer(Timer timer) {
        return appDataBase.timerDao().delete(timer);
    }

    @Override
    public Completable updateTimer(Timer timer) {
        return appDataBase.timerDao().update(timer);
    }

    @Override
    public Flowable<List<Timer>> getAllTimers() {
        return appDataBase.timerDao().getAllTimers();
    }
}
