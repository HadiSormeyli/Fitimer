package com.android.mhs.fitimer;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;

import androidx.core.content.ContextCompat;

import com.android.mhs.fitimer.service.TimerService;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class FitimerApp extends Application {


    public static final String NOTIFICATION_CHANNEL_ID = "NOTIFICATION_CHANNEL_ID";
    public static final String NOTIFICATION_CHANNEL_NAME = "Fitimer NOTIFICATION_CHANNEL_NAME";

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();
//        initService();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID
                    , NOTIFICATION_CHANNEL_NAME
                    , NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(notificationChannel);
        }
    }

    private void initService() {
        Intent startTimerService = new Intent(this, TimerService.class);
        ContextCompat.startForegroundService(this.getApplicationContext(), startTimerService);
    }
}
