package com.android.mhs.fitimer.ui.main.addtimer;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.util.Supplier;
import androidx.lifecycle.ViewModelProvider;


import com.android.mhs.fitimer.R;
import com.android.mhs.fitimer.databinding.FragmentSetRestTimeBinding;
import com.android.mhs.fitimer.ui.base.BaseFragment;
import com.android.mhs.fitimer.ui.base.ViewModelProviderFactory;
import com.android.mhs.fitimer.ui.main.callbacks.Validation;
import com.android.mhs.fitimer.utils.CommonUtils;
import com.android.mhs.fitimer.viewmodel.MainViewModel;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import dagger.hilt.android.AndroidEntryPoint;


@AndroidEntryPoint
public class SetRestTimeFragment extends BaseFragment<FragmentSetRestTimeBinding, MainViewModel> implements Validation {

    public SetRestTimeFragment() {
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_set_rest_time;
    }

    @Override
    public void preformViewModel() {
       viewModel = new ViewModelProvider(activity).get(MainViewModel.class);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void showGuideView() {
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!CommonUtils.isScreenOrationPortrait(activity)) {
            binding.restTimeTv.setVisibility(View.GONE);
            binding.restSecondTv.setVisibility(View.GONE);
            binding.restMinuteTv.setVisibility(View.GONE);
            binding.view.setVisibility(View.GONE);
        }
        setUpNumberPickers();
        setUpNumberPickersValue();
    }

    private void setUpNumberPickersValue() {
        long restTime = viewModel.getRestTime();
        if (restTime != -1) {
            long minute = (restTime % 3600) / 60;
            long second = restTime % 60;
            binding.restSecondNp.setValue((int) second);
            binding.restMinuteNp.setValue((int) minute);
        }
    }

    private void setUpNumberPickers() {
        binding.restSecondNp.setMinValue(0);
        binding.restSecondNp.setMaxValue(59);
        binding.restSecondNp.setValue(15);
        binding.restSecondNp.hasTwoDigitFormatter();

        binding.restMinuteNp.setMinValue(0);
        binding.restMinuteNp.setMaxValue(59);
        binding.restMinuteNp.hasTwoDigitFormatter();
    }

    private long getRestTime() {
        return binding.restMinuteNp.getValue() * 60L + binding.restSecondNp.getValue();
    }

    private boolean validateTime() {
        long restTime = getRestTime();
        if (restTime == 0) {
            Snackbar snackbar = Snackbar.make(binding.getRoot()
                    , getResources().getString(R.string.timer_picker_error)
                    , Snackbar.LENGTH_SHORT);
            View snackBarLayout = snackbar.getView();
            snackBarLayout.setElevation(1000F);
            snackbar.setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE);
            snackbar.show();
            return true;
        }
        viewModel.setRestTime(restTime);
        return false;
    }

    @Override
    public void onDestroyView() {
        viewModel.setRestTime(getRestTime());
        super.onDestroyView();
    }

    @Override
    public boolean validation() {
        return !(validateTime());
    }
}