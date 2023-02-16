package com.android.mhs.fitimer.data.local.prefs;

public interface PreferencesHelper {
    boolean isNight();

    void setNight(boolean isNight);

    String getLanguage();

    void setLanguage(String lan);

    boolean guideIsShowed(String guide);

    boolean isShowUpdateWarning();

    void showUpdateWarning(boolean show);
}
