package com.android.mhs.fitimer.ui.main;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.util.Supplier;
import androidx.lifecycle.ViewModelProvider;


import com.android.mhs.fitimer.R;
import com.android.mhs.fitimer.databinding.FragmentLanguageBinding;
import com.android.mhs.fitimer.data.local.prefs.AppPreferencesHelper;
import com.android.mhs.fitimer.ui.base.BaseBottomSheetFragment;
import com.android.mhs.fitimer.ui.base.ViewModelProviderFactory;
import com.android.mhs.fitimer.viewmodel.MainViewModel;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;


@AndroidEntryPoint
public class LanguageFragment extends BaseBottomSheetFragment<FragmentLanguageBinding, MainViewModel> implements RadioGroup.OnCheckedChangeListener {


    public LanguageFragment() {
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_language;
    }

    @Override
    public void preformViewModel() {
      viewModel = new ViewModelProvider(activity).get(MainViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setToolBar(binding.languageToolBar);
        setUpCheckedButton();
        binding.setOnCheckedChangeListener(this);
    }

    private void setUpCheckedButton() {
        String lan = Locale.getDefault().getLanguage();

        if (lan.equals(AppPreferencesHelper.EN)) {
            binding.englishRadioButton.setChecked(true);
        } else if (lan.equals(AppPreferencesHelper.FA)) {
            binding.persianRadioButton.setChecked(true);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            View bottomSheet = dialog.findViewById(R.id.design_bottom_sheet);
            View view = getView();
            if (view != null) {
                view.post(() -> {
                    View parent = (View) view.getParent();
                    parent.setBackground(null);
                    CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) (parent).getLayoutParams();
                    CoordinatorLayout.Behavior<?> behavior = params.getBehavior();
                    BottomSheetBehavior<?> bottomSheetBehavior = (BottomSheetBehavior<?>) behavior;
                    if (bottomSheetBehavior != null) {
                        bottomSheetBehavior.setPeekHeight(view.getMeasuredHeight());
                        ((View) bottomSheet.getParent()).setBackgroundColor(Color.TRANSPARENT);
                    }
                });
            }
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
        switch (checkedId) {
            case R.id.english_radio_button:
                viewModel.dataManager.setLanguage(AppPreferencesHelper.EN);
                break;

            case R.id.persian_radio_button:
                viewModel.dataManager.setLanguage(AppPreferencesHelper.FA);
                break;
        }
        dismiss();
        activity.recreate();
    }
}