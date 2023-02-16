package com.android.mhs.fitimer.ui.custom.guideview;

import android.graphics.Path;
import android.graphics.RectF;

public interface Targetable {

    Path guidePath();

    RectF boundingRect();
}

