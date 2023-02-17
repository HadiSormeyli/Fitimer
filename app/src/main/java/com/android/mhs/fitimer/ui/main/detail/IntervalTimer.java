package com.android.mhs.fitimer.ui.main.detail;

import android.os.Handler;
import android.os.Looper;


public class IntervalTimer {

    private static final int SECOND = 1000;
    private final Handler handler;
    private final Runnable runnable;
    private final long activeTime;
    private final long restTime;
    private final long repeat;
    private TimerTickListener tickListener;
    private boolean isPause = true;
    private int currentStep = 0;
    private long currentTime = -1;

    public IntervalTimer(long activeTime, long restTime, long repeat) {
        this.activeTime = activeTime;
        this.restTime = restTime;
        this.repeat = 2 * repeat;

        if (currentTime == -1) {
            currentTime = activeTime;
        }

        handler = new Handler(Looper.getMainLooper());
        runnable = new Runnable() {

            @Override
            public void run() {
                if (!isPause) {
                    if (currentStep < IntervalTimer.this.repeat) {
                        if (currentTime > 0) {
                            tickListener.onTick(currentTime);
                            currentTime--;
                        } else {
                            currentStep++;
                            currentTime = (currentStep % 2 == 0) ? activeTime : restTime;
                            tickListener.onNextStep(currentTime, currentStep);
                        }
                        handler.postDelayed(this, SECOND);
                    } else {
                        tickListener.onFinish();
                    }
                }
            }
        };
    }

    public void nextStep() {
        if (currentStep < this.repeat - 1) {
            currentStep++;
            currentTime = (currentStep % 2 == 0) ? activeTime : restTime;
            tickListener.onNextStep(currentTime, currentStep);
        }
    }

    public void previousStep() {
        if (currentStep > 0) {
            currentStep--;
            currentTime = (currentStep % 2 == 0) ? activeTime : restTime;
            tickListener.onNextStep(currentTime, currentStep);
        }
    }

    public void nextFiveSecond() {
        if (currentTime - 5 >= 0) {
            currentTime -= 5;
            tickListener.onTick(currentTime);
        }
    }

    public void previousFiveSecond() {
        long max = (currentStep % 2 == 0) ? activeTime : restTime;
        if (currentTime + 5 <= max) {
            currentTime += 5;
            tickListener.onTick(currentTime);
        }
    }

    public long getRestTime() {
        return restTime;
    }

    public long getActiveTime() {
        return activeTime;
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public void setTickListener(TimerTickListener tickListener) {
        this.tickListener = tickListener;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public boolean isPlaying() {
        return !isPause;
    }

    public void start() {
        isPause = false;
        handler.postDelayed(runnable, SECOND);
    }

    public void pause() {
        isPause = true;
        handler.removeCallbacks(runnable);
    }

    public interface TimerTickListener {
        void onTick(long time);

        void onNextStep(long time, int step);

        void onFinish();
    }
}
