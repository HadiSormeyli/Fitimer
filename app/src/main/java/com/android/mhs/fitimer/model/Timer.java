package com.android.mhs.fitimer.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.android.mhs.fitimer.data.local.db.AppDataBase;

import java.io.Serializable;


@Entity
public class Timer implements Serializable {

    public static final String TIMER = "TIMER";
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String title;
    private long activeTime = 30;
    private long restTime = 15;
    private long repeat = 10;

    @Ignore
    public Timer() {
    }

    public Timer(String title, long activeTime, long restTime, long repeat) {
        this.title = title;
        this.activeTime = activeTime;
        this.restTime = restTime;
        this.repeat = repeat;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getActiveTime() {
        return activeTime;
    }

    public void setActiveTime(long activeTime) {
        this.activeTime = activeTime;
    }

    public long getRestTime() {
        return restTime;
    }

    public void setRestTime(long restTime) {
        this.restTime = restTime;
    }

    public long getRepeat() {
        return repeat;
    }

    public void setRepeat(long repeat) {
        this.repeat = repeat;
    }
}
