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
import androidx.navigation.Navigation;

import com.android.mhs.fitimer.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public abstract class BaseBottomSheetFragment<D extends ViewDataBinding, V extends BaseViewModel> extends BottomSheetDialogFragment {

    protected V viewModel;
    protected D binding;
    protected BaseActivity<?, ?> activity;


    public abstract
    @LayoutRes
    int getLayoutId();

    public abstract void preformViewModel();

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof BaseActivity) {
            BaseActivity<?, ?> activity = (BaseActivity<?, ?>) context;
            this.activity = activity;
            activity.onFragmentAttached();
        }
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            activity.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setToolBar(Toolbar toolBar) {
        activity.setSupportActionBar(toolBar);
        setHasOptionsMenu(true);
    }

    @Override
    public void onDetach() {
        activity = null;
        super.onDetach();
    }

    public void hideKeyboard(View view) {
        if (activity != null) {
            activity.hideKeyboard(view);
        }
    }

    public void navigateTo(int id) {
        Navigation.findNavController(activity, R.id.nav_host_fragment).navigate(id);
    }

    public interface Callback {
        void onFragmentAttached();

        void onFragmentDetached(String tag);
    }
}
