package com.android.mhs.fitimer.ui.base;


import androidx.lifecycle.ViewModel;

import com.android.mhs.fitimer.data.DataManager;
import com.android.mhs.fitimer.utils.rx.SchedulerProvider;

import javax.inject.Inject;

public abstract class BaseViewModel extends ViewModel {

    public DataManager dataManager;
    public SchedulerProvider schedulerProvider;

    public BaseViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        this.dataManager = dataManager;
        this.schedulerProvider = schedulerProvider;
    }
}