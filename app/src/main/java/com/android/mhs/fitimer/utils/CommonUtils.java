package com.android.mhs.fitimer.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;

import java.util.Locale;


public final class CommonUtils {

    private static final char[] ENGLISH_NUMBERS = {'\u0030', '\u0031', '\u0032', '\u0033', '\u0034', '\u0035', '\u0036', '\u0037', '\u0038', '\u0039'};
    private static final char[] PERSIAN_NUMBERS = {'\u06f0', '\u06f1', '\u06f2', '\u06f3', '\u06f4', '\u06f5', '\u06f6', '\u06f7', '\u06f8', '\u06f9'};

    public CommonUtils() {
    }

    public static String convertNumbers(String input) {
        String lang = Locale.getDefault().getLanguage();
        boolean isPersian = "fa".equals(lang) || "ur".equals(lang);
        if (isPersian) return input
                .replace(ENGLISH_NUMBERS[0], PERSIAN_NUMBERS[0])
                .replace(ENGLISH_NUMBERS[1], PERSIAN_NUMBERS[1])
                .replace(ENGLISH_NUMBERS[2], PERSIAN_NUMBERS[2])
                .replace(ENGLISH_NUMBERS[3], PERSIAN_NUMBERS[3])
                .replace(ENGLISH_NUMBERS[4], PERSIAN_NUMBERS[4])
                .replace(ENGLISH_NUMBERS[5], PERSIAN_NUMBERS[5])
                .replace(ENGLISH_NUMBERS[6], PERSIAN_NUMBERS[6])
                .replace(ENGLISH_NUMBERS[7], PERSIAN_NUMBERS[7])
                .replace(ENGLISH_NUMBERS[8], PERSIAN_NUMBERS[8])
                .replace(ENGLISH_NUMBERS[9], PERSIAN_NUMBERS[9]);
        else return input
                .replace(PERSIAN_NUMBERS[0], ENGLISH_NUMBERS[0])
                .replace(PERSIAN_NUMBERS[1], ENGLISH_NUMBERS[1])
                .replace(PERSIAN_NUMBERS[2], ENGLISH_NUMBERS[2])
                .replace(PERSIAN_NUMBERS[3], ENGLISH_NUMBERS[3])
                .replace(PERSIAN_NUMBERS[4], ENGLISH_NUMBERS[4])
                .replace(PERSIAN_NUMBERS[5], ENGLISH_NUMBERS[5])
                .replace(PERSIAN_NUMBERS[6], ENGLISH_NUMBERS[6])
                .replace(PERSIAN_NUMBERS[7], ENGLISH_NUMBERS[7])
                .replace(PERSIAN_NUMBERS[8], ENGLISH_NUMBERS[8])
                .replace(PERSIAN_NUMBERS[9], ENGLISH_NUMBERS[9]);
    }

    public static int removeAlpha(int color) {
        return Color.rgb(Color.red(color), Color.green(color), Color.blue(color));
    }

    public static float dp() {
        return Resources.getSystem().getDisplayMetrics().density;
    }

    public static float sp() {
        return Resources.getSystem().getDisplayMetrics().scaledDensity;
    }

    @SuppressLint("DefaultLocale")
    public static String getTime(long time) {
        String totalTime = "";
        if (time > 3600) {
            long hour = time / 3600;
            totalTime = String.format("%02d:", hour);
        }
        long minute = (time % 3600) / 60;
        long second = time % 60;
        totalTime += String.format("%02d:%02d", minute, second);
        return totalTime;
    }

    public static boolean isScreenOrationPortrait(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }


}