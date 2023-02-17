package com.android.mhs.fitimer.viewmodel;

import androidx.databinding.ObservableField;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.mhs.fitimer.data.DataManager;
import com.android.mhs.fitimer.model.Timer;
import com.android.mhs.fitimer.ui.base.BaseViewModel;
import com.android.mhs.fitimer.utils.Resource;
import com.android.mhs.fitimer.utils.rx.SchedulerProvider;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.Completable;
import io.reactivex.disposables.CompositeDisposable;

@HiltViewModel
public class MainViewModel extends BaseViewModel {

    private final MutableLiveData<Timer> selectedTimer;
    private final MutableLiveData<Boolean> isAddsShown;
    private final ObservableField<String> title;
    private final ObservableField<Long> activeTime;
    private final ObservableField<Long> restTime;
    private final ObservableField<Long> repeat;

    private final MutableLiveData<Resource<List<Timer>>> timers;
    private final MutableLiveData<String> searchText;

    private final CompositeDisposable compositeDisposable;


    @Inject
    public MainViewModel(
            DataManager dataManager
            , SchedulerProvider schedulerProvider
    ) {
        super(dataManager, schedulerProvider);
        selectedTimer = new MutableLiveData<>();
        isAddsShown = new MutableLiveData<>();
        timers = new MutableLiveData<>();
        searchText = new MutableLiveData<>();
        compositeDisposable = new CompositeDisposable();

        title = new ObservableField<>();
        activeTime = new ObservableField<>();
        activeTime.set(30L);
        restTime = new ObservableField<>();
        restTime.set(15L);
        repeat = new ObservableField<>();
        repeat.set(10L);

        fetchData();
    }

    private void fetchData() {
        timers.setValue(Resource.loading());
        compositeDisposable.add(dataManager.getAllTimers()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                        timerList -> timers.setValue(Resource.success(timerList)),
                        throwable -> timers.setValue(Resource.error(throwable.getLocalizedMessage()))
                ));
    }

    public MutableLiveData<String> getSearchText() {
        return searchText;
    }

    public void setSearchText(String text) {
        searchText.setValue(text);
    }

    public LiveData<Resource<List<Timer>>> getTimers() {
        return timers;
    }

    public LiveData<Timer> getSelectedTimer() {
        return selectedTimer;
    }

    public void setSelectedTimer(Timer timer) {
        selectedTimer.setValue(timer);
    }

    public Completable addNewTimer() {
        final Timer timer = new Timer(getTitle(), getActiveTime(), getRestTime(), getRepeat());
        selectedTimer.setValue(timer);
        return dataManager.insertTimer(timer);
    }

    public MutableLiveData<Boolean> getIsAddsShown() {
        return isAddsShown;
    }

    public void setIsAddsShown(Boolean isAddsShown) {
        this.isAddsShown.postValue(isAddsShown);
    }

    public String getTitle() {
        return title.get();
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    public Long getActiveTime() {
        return activeTime.get();
    }

    public void setActiveTime(Long activeTime) {
        this.activeTime.set(activeTime);
    }

    public Long getRestTime() {
        return restTime.get();
    }

    public void setRestTime(Long restTime) {
        this.restTime.set(restTime);
    }

    public Long getRepeat() {
        return repeat.get();
    }

    public void setRepeat(Long repeat) {
        this.repeat.set(repeat);
    }

    public void deleteTimer() {
        dataManager.deleteTimer(selectedTimer.getValue())
                .subscribeOn(schedulerProvider.io())
                .subscribe();
    }
}
