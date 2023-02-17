package com.android.mhs.fitimer.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavDeepLinkBuilder;

import com.android.mhs.fitimer.BuildConfig;
import com.android.mhs.fitimer.FitimerApp;
import com.android.mhs.fitimer.R;
import com.android.mhs.fitimer.model.Timer;
import com.android.mhs.fitimer.ui.main.MainActivity;
import com.android.mhs.fitimer.ui.main.detail.IntervalTimer;
import com.android.mhs.fitimer.ui.main.detail.TimerDetailsFragment;

public class TimerService extends Service implements IntervalTimer.TimerTickListener {

    public static final String MESSAGE = "MESSAGE";
    public static final String ON_TICK = "ON_TICK";
    public static final String NEXT_STEP = "NEXT_STEP";
    public static final String TIMER_ID = "TIMER_ID";
    public static final String TICK = "TICK";
    public static final String STEP = "STEP";

    public static final String NOTIFICATION_PLAY_BUTTON = "NOTIFICATION_PLAY_BUTTON";
    public static final String NOTIFICATION_CLOSE_BUTTON = "NOTIFICATION_CLOSE_BUTTON";
    public static final String NOTIFICATION_MESSAGE = "NOTIFICATION_MESSAGE";
    private static final int NOTIFICATION_ID = 1;
    private static final int NOTIFICATION_PLAY_ID = 2;
    private static final int NOTIFICATION_CLOSE_ID = 3;

    private static final int ALARM_SOUND_START = 3;

    public static TimerService timerService = null;
    private final LocalBinder mBinder = new LocalBinder();
    private final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
    private final NotificationBroadcastReceiver notificationBroadcastReceiver = new NotificationBroadcastReceiver();
    private IntervalTimer intervalTimer;
    private MediaPlayer player;
    private boolean isPlay;
    private Timer timer;
    private RemoteViews notificationLayout;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        timerService = this;
        createNotificationLayout();
        registerReceiver(notificationBroadcastReceiver, new IntentFilter(NotificationBroadcastReceiver.KEY));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @SuppressLint("RemoteViewLayout")
    private void createNotificationLayout() {
        notificationLayout = new RemoteViews(getPackageName(), R.layout.notification_layout);

        Intent playPauseClickedIntent = new Intent(NotificationBroadcastReceiver.KEY);
        playPauseClickedIntent.putExtra(NOTIFICATION_MESSAGE, NOTIFICATION_PLAY_BUTTON);
        PendingIntent playPauseClickedPIntent = PendingIntent.getBroadcast(this, NOTIFICATION_PLAY_ID, playPauseClickedIntent, 0);
        notificationLayout.setOnClickPendingIntent(R.id.notification_play_button, playPauseClickedPIntent);

        Intent closeClickedIntent = new Intent(NotificationBroadcastReceiver.KEY);
        closeClickedIntent.putExtra(NOTIFICATION_MESSAGE, NOTIFICATION_CLOSE_BUTTON);
        PendingIntent closeClickedPIntent = PendingIntent.getBroadcast(this, NOTIFICATION_CLOSE_ID, closeClickedIntent, 0);
        notificationLayout.setOnClickPendingIntent(R.id.notification_close_button, closeClickedPIntent);
    }

    private Notification createNotification(int resource) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(Timer.TIMER, timer);
        PendingIntent pendingIntent = new NavDeepLinkBuilder(this)
                .setComponentName(MainActivity.class)
                .setGraph(R.navigation.nav)
                .setDestination(R.id.timerDetailsFragment)
                .setArguments(bundle)
                .createPendingIntent();

        notificationLayout.setTextViewText(R.id.notification_title_tv, timer.getTitle());
        notificationLayout.setTextViewText(R.id.notification_time_tv, intervalTimer.getCurrentTime() + " / " + getMax());
        notificationLayout.setTextViewText(R.id.notification_step_tv, "(" + (intervalTimer.getCurrentStep() / 2 + 1) + " / " + timer.getRepeat() + ")");
        notificationLayout.setImageViewResource(R.id.notification_play_button, resource);


        return new NotificationCompat.Builder(this, FitimerApp.NOTIFICATION_CHANNEL_ID)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_launcher)
                .setCustomContentView(notificationLayout)
                .setContentIntent(pendingIntent)
                .build();
    }

    private void updateNotification(int playPauseResId) {
        Notification notification = createNotification(playPauseResId);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null)
            notificationManager.notify(NOTIFICATION_ID, notification);
    }

    @Override
    public void onTick(long time) {
        if (time <= ALARM_SOUND_START && !isPlay && intervalTimer.isPlaying()) {
            isPlay = true;
            stopPlayer();
            player = MediaPlayer.create(this, R.raw.alarm);
            player.setLooping(true);
//            player.start(); TODO:uncomment this line
        }

        updateNotification(
                (intervalTimer.isPlaying())
                        ? R.drawable.ic_baseline_pause_24
                        : R.drawable.ic_baseline_play_arrow_24
        );

        Intent timerInfoIntent = new Intent(TimerDetailsFragment.TimerBroadCastReceiver.KEY);
        timerInfoIntent.putExtra(MESSAGE, ON_TICK);
        timerInfoIntent.putExtra(TICK, time);
        timerInfoIntent.putExtra(TIMER_ID, timer.getId());
        localBroadcastManager.sendBroadcast(timerInfoIntent);
    }

    @Override
    public void onNextStep(long time, int step) {
        stopPlayer();
        if (intervalTimer.isPlaying()) updateNotification(R.drawable.ic_baseline_pause_24);
        Intent timerInfoIntent = new Intent(TimerDetailsFragment.TimerBroadCastReceiver.KEY);
        timerInfoIntent.putExtra(MESSAGE, NEXT_STEP);
        timerInfoIntent.putExtra(TICK, time);
        timerInfoIntent.putExtra(STEP, step);
        timerInfoIntent.putExtra(TIMER_ID, timer.getId());
        localBroadcastManager.sendBroadcast(timerInfoIntent);
    }

    public int getStep() {
        if (intervalTimer != null) {
            return intervalTimer.getCurrentStep();
        }
        return 0;
    }

    @Override
    public void onFinish() {
        close();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(notificationBroadcastReceiver);
    }

    public void nexStep() {
        if (intervalTimer != null)
            intervalTimer.nextStep();
    }

    public void previousStep() {
        if (intervalTimer != null)
            intervalTimer.previousStep();
    }

    public Timer getTimer() {
        return timer;
    }

    public void setTimer(Timer timer) {
        pause();

        intervalTimer = new IntervalTimer(timer.getActiveTime(), timer.getRestTime(), timer.getRepeat());
        intervalTimer.setTickListener(this);

        this.timer = timer;
    }

    public long getTime() {
        return intervalTimer.getCurrentTime();
    }

    public void nextFiveSecond() {
        if (intervalTimer != null)
            intervalTimer.nextFiveSecond();
    }

    public void previousFiveSecond() {
        if (intervalTimer != null)
            intervalTimer.previousFiveSecond();
    }

    public void close() {
        pause();
        Intent timerInfoIntent = new Intent(TimerDetailsFragment.TimerBroadCastReceiver.KEY);
        timerInfoIntent.putExtra(MESSAGE, NOTIFICATION_CLOSE_BUTTON);
        timerInfoIntent.putExtra(TIMER_ID, timer.getId());
        localBroadcastManager.sendBroadcast(timerInfoIntent);
        intervalTimer = null;
        timerService = null;
        timer = null;
        stopSelf();
    }

    public boolean isStart() {
        return intervalTimer != null;
    }

    public void pause() {
        stopPlayer();
        if (intervalTimer != null)
            intervalTimer.pause();
        stopForeground(true);
    }

    public void setTick() {
        onTick(intervalTimer.getCurrentTime());
    }

    public long getMax() {
        return (intervalTimer.getCurrentStep() % 2 == 0) ? intervalTimer.getActiveTime() : intervalTimer.getRestTime();
    }

    public int getRepeat() {
        return (int) timer.getRepeat();
    }

    public int getActiveTime() {
        return (int) timer.getActiveTime();
    }

    public int getRestTime() {
        return (int) timer.getRestTime();
    }

    public String getTitle() {
        return timer.getTitle();
    }

    public void start() {
        if (intervalTimer != null)
            intervalTimer.start();

        startForeground(NOTIFICATION_ID, createNotification(R.drawable.ic_baseline_pause_24));
    }

    public boolean isRunning() {
        return intervalTimer != null && intervalTimer.isPlaying();
    }

    private void stopPlayer() {
        if (player != null) {
            isPlay = false;
            player.pause();
            player.stop();
            player.release();
            player = null;
        }
    }

    public boolean checkTimerId(long id) {
        return timer != null && timer.getId() == id;
    }

    public class LocalBinder extends Binder {
        public TimerService getService() {
            return TimerService.this;
        }
    }

    private class NotificationBroadcastReceiver extends BroadcastReceiver {

        public static final String KEY = BuildConfig.APPLICATION_ID + ".NotificationBroadcastReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra(NOTIFICATION_MESSAGE);
            switch (message) {
                case NOTIFICATION_PLAY_BUTTON:
                    if (isRunning()) {
                        Intent timerInfoIntent = new Intent(TimerDetailsFragment.TimerBroadCastReceiver.KEY);
                        timerInfoIntent.putExtra(MESSAGE, NOTIFICATION_PLAY_BUTTON);
                        timerInfoIntent.putExtra(NOTIFICATION_PLAY_BUTTON, false);
                        timerInfoIntent.putExtra(TIMER_ID, timer.getId());
                        localBroadcastManager.sendBroadcast(timerInfoIntent);
                        stopPlayer();
                        intervalTimer.pause();
                        updateNotification(R.drawable.ic_baseline_play_arrow_24);
                    } else {
                        Intent timerInfoIntent = new Intent(TimerDetailsFragment.TimerBroadCastReceiver.KEY);
                        timerInfoIntent.putExtra(MESSAGE, NOTIFICATION_PLAY_BUTTON);
                        timerInfoIntent.putExtra(NOTIFICATION_PLAY_BUTTON, true);
                        timerInfoIntent.putExtra(TIMER_ID, timer.getId());
                        localBroadcastManager.sendBroadcast(timerInfoIntent);
                        start();
                    }
                    break;

                case NOTIFICATION_CLOSE_BUTTON:
                    close();
                    break;
            }
        }
    }
}
