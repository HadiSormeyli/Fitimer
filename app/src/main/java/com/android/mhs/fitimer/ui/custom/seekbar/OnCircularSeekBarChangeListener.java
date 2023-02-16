package com.android.mhs.fitimer.ui.custom.seekbar;

public interface OnCircularSeekBarChangeListener {
    void onProgressChange(CircularSeekBar CircularSeekBar, float progress);

    void onStartTrackingTouch(CircularSeekBar CircularSeekBar);

    void onStopTrackingTouch(CircularSeekBar CircularSeekBar);
}
