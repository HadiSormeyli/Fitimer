package com.android.mhs.fitimer.data.local.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.android.mhs.fitimer.data.local.db.dao.TimerDao;
import com.android.mhs.fitimer.model.Timer;

@Database(entities = {Timer.class}, version = 2, exportSchema = false)
public abstract class AppDataBase extends RoomDatabase {

    public abstract TimerDao timerDao();
}
