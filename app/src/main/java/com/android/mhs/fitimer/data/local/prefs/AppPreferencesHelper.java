package com.android.mhs.fitimer.data.local.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Inject;

public class AppPreferencesHelper implements PreferencesHelper {

    public static final String FA = "fa";
    public static final String EN = "en";
    //guides key
    public static final String ADD_TIMER_GUIDE = "ADD_TIMER_GUIDE";
    public static final String CHANGE_LANGUAGE_GUIDE = "CHANGE_LANGUAGE_GUIDE";
    public static final String CHANGE_THEME_GUIDE = "CHANGE_THEME_GUIDE";
    public static final String TIMER_TITLE_GUIDE = "TIMER_TITLE_GUIDE";
    public static final String NUMBER_PICKER_GUIDE = "NUMBER_PICKER_GUIDE";
    public static final String NEXT_BUTTON_GUIDE = "NEXT_BUTTON_GUIDE";
    public static final String PREV_BUTTON_GUIDE = "PREV_BUTTON_GUIDE";
    public static final String PLAY_BUTTON_GUIDE = "PLAY_BUTTON_GUIDE";
    public static final String RESTART_BUTTON_GUIDE = "RESTART_BUTTON_GUIDE";
    public static final String STEP_VIEW_GUIDE = "STEP_VIEW_GUIDE";
    public static final String SAVE_BUTTON_GUIDE = "SAVE_BUTTON_GUIDE";

    private static final String PREFERENCE_NAME = "FITIMER";
    private static final String IS_NIGHT = "IS_NIGHT";
    private static final String SELECTED_LANGUAGE = "SELECTED_LANGUAGE";
    private static final String SHOW_UPDATE_WARNING = "SHOW_UPDATE_WARNING";

    private final SharedPreferences sharedPreferences;

    @Inject
    public AppPreferencesHelper(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public boolean isNight() {
        return sharedPreferences.getBoolean(IS_NIGHT, true);
    }

    @Override
    public void setNight(boolean isNight) {
        sharedPreferences.edit()
                .putBoolean(IS_NIGHT, isNight)
                .apply();
    }

    @Override
    public String getLanguage() {
        return sharedPreferences.getString(SELECTED_LANGUAGE, EN);
    }

    @Override
    public void setLanguage(String lan) {
        sharedPreferences.edit()
                .putString(SELECTED_LANGUAGE, lan)
                .apply();
    }

    @Override
    public boolean guideIsShowed(String guide) {
        boolean isShow = sharedPreferences.getBoolean(guide, false);
        if (!isShow) guideShowed(guide);
        return isShow;
    }

    @Override
    public boolean isShowUpdateWarning() {
        return sharedPreferences.getBoolean(SHOW_UPDATE_WARNING, true);
    }

    @Override
    public void showUpdateWarning(boolean show) {
        sharedPreferences.edit().putBoolean(SHOW_UPDATE_WARNING, show).apply();
    }

    public void guideShowed(String guide) {
        sharedPreferences.edit().putBoolean(guide, true).apply();
    }
}
