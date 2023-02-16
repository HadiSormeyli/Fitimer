package com.android.mhs.fitimer.utils;

import android.annotation.SuppressLint;
import android.widget.TextView;

import androidx.databinding.BindingAdapter;

import com.android.mhs.fitimer.model.Timer;


public final class BindingUtils {

    @SuppressLint("DefaultLocale")
    @BindingAdapter("android:time")
    public static void setTime(TextView textView, long time) {
        textView.setText(CommonUtils.getTime(time));
    }

    @SuppressLint("DefaultLocale")
    @BindingAdapter("android:totalTime")
    public static void setTotalTime(TextView textView, Timer timer) {
        textView.setText(CommonUtils.getTime((timer.getActiveTime() + timer.getRestTime()) * timer.getRepeat()));
    }

    @SuppressLint("DefaultLocale")
    @BindingAdapter("android:repeat")
    public static void setRepeat(TextView textView, long repeat) {
        textView.setText(String.format("%d", repeat));
    }

    @SuppressLint("DefaultLocale")
    @BindingAdapter("android:timerTitle")
    public static void setTimerTitle(TextView textView, String title) {
        textView.setText(CommonUtils.convertNumbers(title));
    }
}
