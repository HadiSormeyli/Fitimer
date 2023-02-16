package com.android.mhs.fitimer.ui.main.detail;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.mhs.fitimer.BuildConfig;
import com.android.mhs.fitimer.R;
import com.android.mhs.fitimer.data.local.prefs.AppPreferencesHelper;
import com.android.mhs.fitimer.databinding.FragmentTimerDetailsBinding;
import com.android.mhs.fitimer.model.Timer;
import com.android.mhs.fitimer.service.TimerService;
import com.android.mhs.fitimer.ui.base.BaseFragment;
import com.android.mhs.fitimer.ui.custom.guideview.DismissType;
import com.android.mhs.fitimer.ui.custom.guideview.Gravity;
import com.android.mhs.fitimer.ui.custom.guideview.GuideView;
import com.android.mhs.fitimer.ui.custom.guideview.PointerType;
import com.android.mhs.fitimer.utils.CommonUtils;
import com.android.mhs.fitimer.utils.rx.SchedulerProvider;
import com.android.mhs.fitimer.viewmodel.MainViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TimerDetailsFragment extends BaseFragment<FragmentTimerDetailsBinding, MainViewModel> implements View.OnClickListener {

    private final TimerBroadCastReceiver broadCastReceiver = new TimerBroadCastReceiver();
    private TimerService timerService;
    private Timer timer;
    private boolean serviceBounded;
    private boolean isRequestToPlay = false;
    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            timerService = null;
            serviceBounded = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TimerService.LocalBinder mLocalBinder = (TimerService.LocalBinder) service;
            timerService = mLocalBinder.getService();
            serviceBounded = true;
            if (isRequestToPlay) {
                if (!timerService.checkTimerId(timer.getId())) {
                    timerService.setTimer(timer);
                }
                binding.pauseTimerButton.setSelected(true);
                timerService.start();
                isRequestToPlay = false;
            }

            updateUI();
        }
    };

    public TimerDetailsFragment() {
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_timer_details;
    }

    @Override
    public void preformViewModel() {
        viewModel = new ViewModelProvider(activity).get(MainViewModel.class);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void showGuideView() {
        builder = new GuideView.Builder(activity)
                .setTargetView(binding.pauseTimerButton)
                .setRADIUS_SIZE_TARGET_RECT(16)
                .setTitles(getResources().getStringArray(R.array.timer_details_title_guide))
                .setContentTexts(getResources().getStringArray(R.array.timer_details_description_guide))
                .setMultiView(true)
                .setGuideListener(view -> {
                    switch (view.getId()) {
                        case R.id.pause_timer_button:
                            builder.setTargetView(binding.restartTimerButton);
                            break;

                        case R.id.restart_timer_button:
                            if (!CommonUtils.isScreenOrationPortrait(activity))
                                builder.setRADIUS_SIZE_TARGET_RECT(0);
                            builder.setTargetView(binding.stepViewContainer);
                            break;

                        case R.id.step_view_container:
                            builder.setTargetView(binding.deleteTimer)
                                    .setRADIUS_SIZE_TARGET_RECT(8);
                            break;

                        case R.id.delete_timer:
                            guideView = null;
                            builder = null;
                            break;
                    }

                    if (builder != null)
                        guideView = builder.build();
                })
                .keys(new String[]{AppPreferencesHelper.PLAY_BUTTON_GUIDE, AppPreferencesHelper.RESTART_BUTTON_GUIDE
                        , AppPreferencesHelper.STEP_VIEW_GUIDE, AppPreferencesHelper.SAVE_BUTTON_GUIDE})
                .setGravity(Gravity.center)
                .setPointerType(PointerType.circle)
                .setDismissType(DismissType.anywhere);

        guideView = builder.build();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        viewModel.getIsAddsShown().observe(getViewLifecycleOwner(), isShown -> {
            if (isShown) {
                if (CommonUtils.isScreenOrationPortrait(activity))
                    binding.timerSeekbar.setArcRadius(85 * CommonUtils.dp());
                else binding.timerSeekbar.setArcRadius(70 * CommonUtils.dp());
            }
        });

        binding.timerDetailsToolbar.setTitle("");
        setToolBar(binding.timerDetailsToolbar);
        binding.setOnClickListener(this);

        viewModel.getSelectedTimer().observe(getViewLifecycleOwner(), timer -> {
            TimerDetailsFragment.this.timer = timer;
            setUpUI();
        });


        if (getArguments() != null) {
            Timer timer = (Timer) getArguments().getSerializable(Timer.TIMER);
            viewModel.setSelectedTimer(timer);
        }
    }

    private void updateUI() {
        if (timerService.checkTimerId(timer.getId())) {
            timerService.setTick();
            binding.pauseTimerButton.setSelected(timerService.isRunning());
            binding.restartTimerButton.setEnabled(true);
            setStep(timerService.getStep());
        }
    }

    @SuppressLint("SetTextI18n")
    private void setUpUI() {
        if (timer != null) {
            setUpSeekBar(timer);
            binding.timerDetailsToolbar.setTitle(CommonUtils.convertNumbers(timer.getTitle()));
            binding.restartTimerButton.setEnabled(false);
            binding.pauseTimerButton.setSelected(false);
            binding.timerTextView.setText(CommonUtils.getTime(timer.getActiveTime()));
            binding.totalTimerTv.setText(activity.getResources().getString(R.string.total) + " " + CommonUtils.getTime(timer.getRepeat()
                    * (timer.getRestTime() + timer.getActiveTime())));
            setUpStepView(timer);
        }
    }

    private void setUpSeekBar(Timer timer) {
        binding.timerSeekbar.onChangeStep(true);
        binding.timerSeekbar.setMAX((int) timer.getActiveTime());
    }

    @SuppressLint("DefaultLocale")
    private void setUpStepView(Timer timer) {
        final List<String> steps = new ArrayList<>();

        final String active = CommonUtils.getTime(timer.getActiveTime());
        final String rest = CommonUtils.getTime(timer.getRestTime());

        for (int i = 0; i < timer.getRepeat(); i++) {
            steps.add(activity.getResources().getString(R.string.active_time) + String.format(" %d", (i + 1)) + " : " + active);
            steps.add(activity.getResources().getString(R.string.rest_time) + String.format(" %d", (i + 1)) + " : " + rest);
        }

        binding.timerStepView.reverseDraw(false)
                .setStepsViewIndicatorCompletingPosition(1)
                .setStepViewTexts(steps)
                .setLinePaddingProportion(0.85f);

        binding.scrollView.post(() -> binding.scrollView.smoothScrollTo(0, 0));
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.pause_timer_button:
                playOrPause(!view.isSelected());
                break;

            case R.id.restart_timer_button:
                if (timerService != null)
                    timerService.close();
                break;

            case R.id.delete_timer:
                viewModel.deleteTimer();
                activity.onBackPressed();
                break;

            case R.id.next_step_timer_bu:
                if (timerService != null)
                    timerService.nexStep();
                break;

            case R.id.previous_step_timer_bu:
                if (timerService != null)
                    timerService.previousStep();
                break;

            case R.id.next_five_second_timer_bu:
                if (timerService != null)
                    timerService.nextFiveSecond();
                break;

            case R.id.previous_five_second_timer_bu:
                if (timerService != null)
                    timerService.previousFiveSecond();
                break;
        }
    }

    private void playOrPause(boolean isSelected) {
        startService();
        if (!binding.restartTimerButton.isEnabled())
            binding.restartTimerButton.setEnabled(true);

        if (isSelected) {
            if (timerService != null && timer != null) {
                if (!timerService.checkTimerId(timer.getId())) {
                    timerService.setTimer(timer);
                }
                binding.pauseTimerButton.setSelected(true);
                timerService.start();
            } else isRequestToPlay = true;
        } else {
            binding.pauseTimerButton.setSelected(false);
            timerService.pause();
        }
    }

    private void restart() {
        unBindService();
        binding.timerSeekbar.onChangeStep(true);
        binding.restartTimerButton.setEnabled(false);
        binding.pauseTimerButton.setSelected(false);
        binding.timerTextView.setText(CommonUtils.getTime(timer.getActiveTime()));
        binding.timerSeekbar.setMAX((int) timer.getActiveTime());
        binding.timerStepView.setStepsViewIndicatorCompletingPosition(1);
        binding.scrollView.post(() -> binding.scrollView.smoothScrollTo(0, 0));
    }

    public void onTick(long time) {
        binding.timerSeekbar.setProgress(time);
        binding.timerTextView.setText(CommonUtils.getTime(time));
    }

    public void onNextStep(long time, int step) {
        binding.timerTextView.setText(CommonUtils.getTime(time));
        binding.timerSeekbar.setMAX((int) time);
        setStep(step);
    }

    private void setStep(int step) {
        binding.timerSeekbar.onChangeStep(step % 2 == 0);
        binding.timerStepView.setStepsViewIndicatorCompletingPosition(step + 1);
        binding.scrollView.post(() -> binding.scrollView.smoothScrollTo(0, (int) ((step * 56 + 12) * CommonUtils.dp())));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        activity.getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public void startService() {
        if (timerService == null) {
            Intent intent = new Intent(activity, TimerService.class);
            if (TimerService.timerService == null) {
                ContextCompat.startForegroundService(activity.getApplicationContext(), intent);
            }
            activity.getApplicationContext().bindService(intent, connection, Context.BIND_AUTO_CREATE);
        }
    }

    public void unBindService() {
        if (serviceBounded) {
            timerService = null;
            serviceBounded = false;
            activity.getApplicationContext().unbindService(connection);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (timerService == null)
            startService();

        LocalBroadcastManager.getInstance(activity).registerReceiver(broadCastReceiver, new IntentFilter(TimerBroadCastReceiver.KEY));
    }

    @Override
    public void onPause() {
        super.onPause();
        if (serviceBounded)
            unBindService();

        LocalBroadcastManager.getInstance(activity).unregisterReceiver(broadCastReceiver);
    }

    public class TimerBroadCastReceiver extends BroadcastReceiver {

        public static final String KEY = BuildConfig.APPLICATION_ID + ".TimerBroadCastReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            if (timer.getId() == intent.getLongExtra(TimerService.TIMER_ID, -1)) {
                switch (intent.getStringExtra(TimerService.MESSAGE)) {
                    case TimerService.ON_TICK:
                        onTick(intent.getLongExtra(TimerService.TICK, 0));
                        break;

                    case TimerService.NEXT_STEP:
                        onNextStep(intent.getLongExtra(TimerService.TICK, 0), intent.getIntExtra(TimerService.STEP, 0));
                        break;

                    case TimerService.NOTIFICATION_PLAY_BUTTON:
                        binding.pauseTimerButton.setSelected(intent.getBooleanExtra(TimerService.NOTIFICATION_PLAY_BUTTON, false));
                        break;

                    case TimerService.NOTIFICATION_CLOSE_BUTTON:
                        restart();
                        break;
                }
            }
        }
    }
}