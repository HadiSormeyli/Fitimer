package com.android.mhs.fitimer.ui.base;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.android.mhs.fitimer.ui.custom.guideview.GuideView;


public abstract class BaseFragment<D extends ViewDataBinding, V extends BaseViewModel> extends Fragment {

    protected V viewModel;
    protected D binding;
    protected BaseActivity activity;
    protected GuideView guideView = null;
    protected GuideView.Builder builder = null;

    public abstract
    @LayoutRes
    int getLayoutId();

    public abstract void preformViewModel();

    public void hideGuideView() {
        if (guideView != null) guideView.dismiss();
    }

    public boolean isGuideViewShown() {
        return guideView != null && guideView.isShowing();
    }

    public abstract void showGuideView();

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.activity = (BaseActivity) context;
        activity.onFragmentAttached();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Navigation.findNavController(binding.getRoot()).popBackStack();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false);
        preformViewModel();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        showGuideView();
    }

    public void setToolBar(Toolbar toolBar) {
        activity.setSupportActionBar(toolBar);
        setHasOptionsMenu(true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    public void hideKeyboard(View view) {
        if (activity != null) {
            activity.hideKeyboard(view);
        }
    }

    public void showKeyboard(View view) {
        if (activity != null) {
            activity.showKeyboard(view);
        }
    }

    public void navigateTo(int id) {
        Navigation.findNavController(binding.getRoot()).navigate(id);
    }

    public interface Callback {
        void onFragmentAttached();

        void onFragmentDetached(String tag);
    }
}