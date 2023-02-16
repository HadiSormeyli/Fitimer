package com.android.mhs.fitimer.data.local.db;

import com.android.mhs.fitimer.model.Timer;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;

public interface DbHelper {

    Completable insertTimer(Timer timer);

    Completable deleteTimer(Timer timer);

    Completable updateTimer(Timer timer);

    Flowable<List<Timer>> getAllTimers();
}
