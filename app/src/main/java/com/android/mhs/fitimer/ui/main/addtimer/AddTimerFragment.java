package com.android.mhs.fitimer.ui.main.addtimer;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;

import com.android.mhs.fitimer.R;
import com.android.mhs.fitimer.databinding.FragmentAddTimerBinding;
import com.android.mhs.fitimer.model.Timer;
import com.android.mhs.fitimer.ui.base.BaseFragment;
import com.android.mhs.fitimer.ui.base.FragmentAdapter;
import com.android.mhs.fitimer.ui.main.callbacks.Validation;
import com.android.mhs.fitimer.ui.main.detail.TimerDetailsFragment;
import com.android.mhs.fitimer.viewmodel.MainViewModel;
import com.google.android.material.color.MaterialColors;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.CompletableObserver;
import io.reactivex.disposables.Disposable;

@AndroidEntryPoint
public class AddTimerFragment extends BaseFragment<FragmentAddTimerBinding, MainViewModel> implements View.OnClickListener {

    private FragmentAdapter fragmentAdapter;

    public AddTimerFragment() {
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_add_timer;
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

        setToolBar(binding.addTimerToolbar);
        binding.setOnClickListener(this);
        setUpViewPager();
    }


    private void setUpViewPager() {

        List<Fragment> fragments = getChildFragmentManager().getFragments();
        if (fragments.size() == 0) {
            fragments = new ArrayList<>();
            fragments.add(new SetActiveTimeFragment());
            fragments.add(new SetRestTimeFragment());
            fragments.add(new SetRepeatFragment());
        }
        fragmentAdapter = new FragmentAdapter(getChildFragmentManager(), getLifecycle(), fragments);
        binding.timerViewPager.setUserInputEnabled(false);
        binding.timerViewPager.setOffscreenPageLimit(2);
        binding.timerViewPager.setAdapter(fragmentAdapter);

        final String[] titles = activity.getResources().getStringArray(R.array.add_timer_titles);
        binding.timerViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                if (position == 0) {
                    binding.prevButton.setEnabled(false);
                    binding.prevButton.setTextColor(ContextCompat.getColor(activity, R.color.grey_800));
                } else if (position == fragmentAdapter.getItemCount() - 1) {
                    binding.nextButton.setText(activity.getResources().getString(R.string.finish));
                } else {
                    binding.nextButton.setText(activity.getResources().getString(R.string.next));
                    binding.prevButton.setEnabled(true);
                    binding.prevButton.setTextColor(MaterialColors.getColor(binding.prevButton, R.attr.colorControlNormal));
                }
                binding.addTimerToolbar.setTitle(titles[position]);
            }
        });
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.next_button:
                nextPage();
                break;
            case R.id.prev_button:
                binding.timerViewPager.setCurrentItem(binding.timerViewPager.getCurrentItem() - 1);
                break;
        }
    }

    private void nextPage() {
        int position = binding.timerViewPager.getCurrentItem();
        Validation validation = (Validation) fragmentAdapter.createFragment(position);
        boolean validated = validation.validation();
        if (position == fragmentAdapter.getItemCount() - 1) {
            viewModel.addNewTimer()
                    .subscribeOn(viewModel.schedulerProvider.io())
                    .observeOn(viewModel.schedulerProvider.ui())
                    .subscribe(new CompletableObserver() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onComplete() {
                            Navigation.findNavController(binding.getRoot()).popBackStack();
                            navigateTo(R.id.action_mainFragment_to_timerDetailsFragment);
                        }

                        @Override
                        public void onError(Throwable e) {
                        }
                    });
        } else if (validated) binding.timerViewPager.setCurrentItem(position + 1);
    }

    @Override
    public boolean isGuideViewShown() {
        BaseFragment baseFragment = (BaseFragment) fragmentAdapter.getItem(0);
        final boolean isShown = baseFragment.isGuideViewShown();
        if (isShown) baseFragment.hideGuideView();
        return isShown;
    }

    @Override
    public void onDestroyView() {
        hideKeyboard(binding.nextButton);
        super.onDestroyView();
    }
}