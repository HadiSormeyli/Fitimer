package com.android.mhs.fitimer.ui.main.addtimer;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.android.mhs.fitimer.R;
import com.android.mhs.fitimer.data.local.prefs.AppPreferencesHelper;
import com.android.mhs.fitimer.databinding.FragmentSetActiveTimeBinding;
import com.android.mhs.fitimer.ui.base.BaseFragment;
import com.android.mhs.fitimer.ui.custom.guideview.DismissType;
import com.android.mhs.fitimer.ui.custom.guideview.Gravity;
import com.android.mhs.fitimer.ui.custom.guideview.GuideView;
import com.android.mhs.fitimer.ui.custom.guideview.PointerType;
import com.android.mhs.fitimer.ui.main.callbacks.Validation;
import com.android.mhs.fitimer.ui.main.detail.TimerDetailsFragment;
import com.android.mhs.fitimer.utils.CommonUtils;
import com.android.mhs.fitimer.viewmodel.MainViewModel;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SetActiveTimeFragment extends BaseFragment<FragmentSetActiveTimeBinding, MainViewModel> implements Validation {


    public SetActiveTimeFragment() {
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_set_active_time;
    }

    @Override
    public void preformViewModel() {
        viewModel = new ViewModelProvider(activity).get(MainViewModel.class);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void showGuideView() {
        builder = new GuideView.Builder(activity)
                .setTargetView(activity.findViewById(R.id.next_button))
                .setTitles(getResources().getStringArray(R.array.add_timer_title_guide))
                .setContentTexts(getResources().getStringArray(R.array.add_timer_description_guide))
                .setMultiView(true)
                .setGuideListener(view -> {
                    switch (view.getId()) {
                        case R.id.next_button:
                            builder.setTargetView(activity.findViewById(R.id.prev_button));
                            break;

                        case R.id.prev_button:
                            builder.setTargetView(binding.timerTitleEt);
                            break;

                        case R.id.timer_title_et:
                            builder.setTargetView(binding.numberPickerLayout);
                            break;

                        case R.id.number_picker_layout:
                            guideView = null;
                            builder = null;
                            break;
                    }

                    if (builder != null)
                        guideView = builder.build();
                })
                .keys(new String[]{AppPreferencesHelper.NEXT_BUTTON_GUIDE, AppPreferencesHelper.PREV_BUTTON_GUIDE
                        , AppPreferencesHelper.TIMER_TITLE_GUIDE, AppPreferencesHelper.NUMBER_PICKER_GUIDE})
                .setGravity(Gravity.center)
                .setPointerType(PointerType.circle)
                .setDismissType(DismissType.anywhere);

        guideView = builder.build();
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!CommonUtils.isScreenOrationPortrait(activity)) {
            binding.activeTimeTv.setVisibility(View.GONE);
            binding.activeSecondTv.setVisibility(View.GONE);
            binding.activeMinuteTv.setVisibility(View.GONE);
        }

        if (savedInstanceState == null) {
            int size = 0;
            if (viewModel.getTimers().getValue() != null)
                if (viewModel.getTimers().getValue().data != null)
                    size = viewModel.getTimers().getValue().data.size();

            String title = String.format("%d", size + 1);
            viewModel.setTitle(title);
            binding.timerTitleEt.setText(title);
        } else binding.timerTitleEt.setText(viewModel.getTitle());


        setUpNumberPickers();
        setUpNumberPickersValue();
    }

    private void setUpNumberPickersValue() {
        long activeTime = viewModel.getActiveTime();
        if (activeTime != -1) {
            long minute = (activeTime % 3600) / 60;
            long second = activeTime % 60;
            binding.activeSecondNp.setValue((int) second);
            binding.activeMinuteNp.setValue((int) minute);
        }
    }

    private void setUpNumberPickers() {
        binding.activeSecondNp.setMinValue(0);
        binding.activeSecondNp.setMaxValue(59);
        binding.activeSecondNp.setValue(30);
        binding.activeSecondNp.hasTwoDigitFormatter();

        binding.activeMinuteNp.setMinValue(0);
        binding.activeMinuteNp.setMaxValue(59);
        binding.activeMinuteNp.hasTwoDigitFormatter();
    }

    private boolean validateTitle() {
        String title = "";
        if (binding.timerTitleEt.getText() != null) {
            title = binding.timerTitleEt.getText().toString();
            if (TextUtils.isEmpty(title)) {
                binding.timerTitleEt.setError(activity.getResources().getString(R.string.title_error));
                return true;
            }
        }
        viewModel.setTitle(title);
        return false;
    }

    private long getActiveTime() {
        return binding.activeMinuteNp.getValue() * 60L + binding.activeSecondNp.getValue();
    }

    private boolean validateTime() {
        long activeTime = getActiveTime();
        if (activeTime == 0) {
            Snackbar snackbar = Snackbar.make(binding.getRoot()
                    , getResources().getString(R.string.timer_picker_error)
                    , Snackbar.LENGTH_SHORT);
            View snackBarLayout = snackbar.getView();
            snackBarLayout.setElevation(1000F);
            snackbar.setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE);
            snackbar.show();
            return true;
        }
        viewModel.setActiveTime(activeTime);
        return false;
    }

    @Override
    public void onDestroyView() {
        viewModel.setActiveTime(getActiveTime());
        viewModel.setTitle(binding.timerTitleEt.getText().toString());
        super.onDestroyView();
    }

    @Override
    public boolean validation() {
        return !(validateTitle() | validateTime());
    }
}