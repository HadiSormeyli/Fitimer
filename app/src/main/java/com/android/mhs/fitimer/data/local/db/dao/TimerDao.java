package com.android.mhs.fitimer.data.local.db.dao;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.android.mhs.fitimer.model.Timer;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;

@Dao
public interface TimerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insert(Timer timer);

    @Delete
    Completable delete(Timer timer);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    Completable update(Timer timer);

    @Query("SELECT * FROM timer ORDER BY id DESC")
    Flowable<List<Timer>> getAllTimers();
}
