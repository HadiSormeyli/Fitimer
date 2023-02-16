package com.android.mhs.fitimer.ui.main.addtimer;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.android.mhs.fitimer.R;
import com.android.mhs.fitimer.databinding.FragmentSetRepeatBinding;
import com.android.mhs.fitimer.ui.base.BaseFragment;
import com.android.mhs.fitimer.ui.main.callbacks.Validation;
import com.android.mhs.fitimer.utils.CommonUtils;
import com.android.mhs.fitimer.viewmodel.MainViewModel;

import dagger.hilt.android.AndroidEntryPoint;


@AndroidEntryPoint
public class SetRepeatFragment extends BaseFragment<FragmentSetRepeatBinding, MainViewModel> implements Validation {


    public SetRepeatFragment() {
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_set_repeat;
    }

    @Override
    public void preformViewModel() {
        viewModel = new ViewModelProvider(activity).get(MainViewModel.class);
    }

    @Override
    public void showGuideView() {

    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!CommonUtils.isScreenOrationPortrait(activity)) {
            binding.repeatTimeTv.setVisibility(View.GONE);
            binding.view.setVisibility(View.GONE);
        }
        setUpNumberPicker();
        setUpNumberPickersValue();
    }

    private void setUpNumberPickersValue() {
        long repeat = viewModel.getRepeat();
        if (repeat != -1) {
            binding.repeatTimerNp.setValue((int) repeat);
        }
    }

    private void setUpNumberPicker() {
        binding.repeatTimerNp.setMinValue(1);
        binding.repeatTimerNp.setMaxValue(50);
    }

    @Override
    public void onDestroyView() {
        viewModel.setRepeat((long) binding.repeatTimerNp.getValue());
        super.onDestroyView();
    }

    @Override
    public boolean validation() {
        viewModel.setRepeat((long) binding.repeatTimerNp.getValue());
        return true;
    }
}