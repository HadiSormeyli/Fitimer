package com.android.mhs.fitimer.ui.main;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Filter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.text.TextUtilsCompat;
import androidx.core.view.ViewCompat;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.Navigation;

import com.airbnb.lottie.LottieAnimationView;
import com.android.mhs.fitimer.R;
import com.android.mhs.fitimer.data.local.prefs.AppPreferencesHelper;
import com.android.mhs.fitimer.databinding.FragmentMainBinding;
import com.android.mhs.fitimer.model.Timer;
import com.android.mhs.fitimer.service.TimerService;
import com.android.mhs.fitimer.ui.base.BaseFragment;
import com.android.mhs.fitimer.ui.custom.guideview.DismissType;
import com.android.mhs.fitimer.ui.custom.guideview.GuideView;
import com.android.mhs.fitimer.ui.custom.guideview.PointerType;
import com.android.mhs.fitimer.ui.main.callbacks.OnTimerClick;
import com.android.mhs.fitimer.ui.main.detail.TimerDetailsFragment;
import com.android.mhs.fitimer.utils.CommonUtils;
import com.android.mhs.fitimer.viewmodel.MainViewModel;

import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainFragment extends BaseFragment<FragmentMainBinding, MainViewModel> implements OnTimerClick
        , View.OnClickListener, Filter.FilterListener {


    private TimersAdapter timersAdapter;
    private LottieAnimationView lottieAnimationView;
    private TimerService timerService;
    private final BroadcastReceiver mainTimerBroadcastReceiver = new BroadcastReceiver() {

        @SuppressLint({"DefaultLocale", "SetTextI18n"})
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getStringExtra(TimerService.MESSAGE)) {
                case TimerService.ON_TICK:
                    long time = intent.getLongExtra(TimerService.TICK, 0);
                    binding.runningTimerSeekbar.animateTo(time);
                    binding.runningTimerTv.setText(CommonUtils.getTime(time));
                    break;

                case TimerService.NEXT_STEP:
                    long stepTime = intent.getLongExtra(TimerService.TICK, 0);
                    long step = intent.getIntExtra(TimerService.STEP, 0);
                    binding.runningTimerTv.setText(CommonUtils.getTime(stepTime));
                    binding.runningTimerSeekbar.onChangeStep(step % 2 == 0);
                    binding.runningTimerSeekbar.setMAX((int) stepTime);
                    binding.runningTimerStepTv.setText("(" + String.format("%d / %d", timerService.getStep() / 2 + 1, timerService.getRepeat()) + ")");
                    break;

                case TimerService.NOTIFICATION_CLOSE_BUTTON:
                    closeRunningTimerLayout();
                    break;

                case TimerService.NOTIFICATION_PLAY_BUTTON:
                    binding.pauseRunningTimerBu.setSelected(intent.getBooleanExtra(TimerService.NOTIFICATION_PLAY_BUTTON, false));
                    break;
            }
        }
    };
    private boolean serviceBounded;
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

            updateUI();
        }
    };

    public MainFragment() {
    }

    @SuppressLint("SetTextI18n")
    private void updateUI() {
        binding.runningTimerLayout.setEnabled(true);
        binding.runningTimerLayout.setVisibility(View.VISIBLE);
        timerService.setTick();
        binding.runningTimerSeekbar.onChangeStep(timerService.getStep() % 2 == 0);
        binding.runningTimerTitleTv.setText(timerService.getTitle());
        binding.runningTimerSeekbar.setMAX((int) timerService.getMax());
        binding.pauseRunningTimerBu.setSelected(timerService.isRunning());
        binding.runningTimerStepTv.setText("(" + (timerService.getStep() / 2 + 1) + " / " + timerService.getRepeat() + ")");
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_main;
    }

    @Override
    public void preformViewModel() {
        viewModel = new ViewModelProvider(activity).get(MainViewModel.class);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void showGuideView() {
        builder = new GuideView.Builder(activity)
                .setTargetView(binding.addTimerLayout)
                .setRADIUS_SIZE_TARGET_RECT(16)
                .setTitles(getResources().getStringArray(R.array.main_fragment_title_guide))
                .setContentTexts(getResources().getStringArray(R.array.main_fragment_description_guide))
                .setMultiView(true)
                .setGuideListener(view -> {
                    switch (view.getId()) {
                        case R.id.add_timer_layout:
                            builder.setRADIUS_SIZE_TARGET_RECT(8);
                            builder.setTargetView(binding.changeThemeIv);
                            break;
                        case R.id.change_theme_iv:
                            builder.setTargetView(binding.changeLanguageIv);
                            break;
                        case R.id.change_language_iv:
                            guideView = null;
                            builder = null;
                            break;
                    }
                    if (builder != null)
                        guideView = builder.build();
                })
                .keys(new String[]{AppPreferencesHelper.ADD_TIMER_GUIDE
                        , AppPreferencesHelper.CHANGE_THEME_GUIDE, AppPreferencesHelper.CHANGE_LANGUAGE_GUIDE})
                .setGravity(com.android.mhs.fitimer.ui.custom.guideview.Gravity.auto)
                .setPointerType(PointerType.circle)
                .setDismissType(DismissType.anywhere);

        guideView = builder.build();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        if (TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == ViewCompat.LAYOUT_DIRECTION_LTR) {
            binding.mainToolBar.setPadding((int) (40 * CommonUtils.dp()), 0, 0, 0);
        } else binding.mainToolBar.setPadding(0, 0, (int) (80 * CommonUtils.dp()), 0);

        setUpSearchAnimation();
        setUpAddButton();
        binding.setOnClickListener(this);
        observeData();
        binding.searchTextLayout.setEndIconOnClickListener(v -> hideKeyboard(binding.searchEditText));
        binding.searchTextLayout.setStartIconOnClickListener(v -> toggleToolBar());
    }

    private void observeData() {
        timersAdapter = new TimersAdapter(MainFragment.this);
        binding.timersRecycleView.setAdapter(timersAdapter);

        viewModel.getTimers().observe(getViewLifecycleOwner(), data -> {
            switch (data.status) {
                case LOADING:
                    break;
                case SUCCESS:
                    timersAdapter.submitData(data.data);

                    if (data.data.size() == 0)
                        binding.noTimerTv.setVisibility(View.VISIBLE);
                    break;
                case ERROR:
                    binding.noTimerTv.setVisibility(View.VISIBLE);
                    break;
            }
        });

        setUpSearchLayout();


        viewModel.getSearchText().observe(getViewLifecycleOwner(), text -> {
            if (text != null && !TextUtils.isEmpty(text)) {
                binding.searchCardView.setVisibility(View.VISIBLE);
            }
            timersAdapter.getFilter().filter(text, MainFragment.this);
        });
    }

    private void setUpSearchAnimation() {
        lottieAnimationView = new LottieAnimationView(activity);
        CoordinatorLayout.LayoutParams layoutParams
                = new CoordinatorLayout.LayoutParams(CoordinatorLayout.LayoutParams.WRAP_CONTENT
                , CoordinatorLayout.LayoutParams.WRAP_CONTENT);

        layoutParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;

        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            lottieAnimationView.setAnimation(R.raw.night_lottie_animation);
        } else lottieAnimationView.setAnimation(R.raw.light_lottie_animation);

        lottieAnimationView.setSpeed(1);
        lottieAnimationView.loop(true);
        lottieAnimationView.setLayoutParams(layoutParams);
        lottieAnimationView.setVisibility(View.GONE);
        binding.mainFragmentContainer.addView(lottieAnimationView);
    }

    private void setUpSearchLayout() {
        binding.searchCardView.setVisibility(View.GONE);
        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                viewModel.setSearchText(editable.toString());
            }
        });
    }

    private void setUpAddButton() {
        binding.mainScrollView.setOnScrollChangeListener(
                (NestedScrollView.OnScrollChangeListener)
                        (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                            if (scrollY > oldScrollY && oldScrollY != 0) {
                                binding.addTextView.setVisibility(View.GONE);
                            } else binding.addTextView.setVisibility(View.VISIBLE);
                        });
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.change_theme_iv:
                activity.overridePendingTransition(0, 0);
                viewModel.dataManager.setNight(AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_YES);
                activity.recreate();
                break;
            case R.id.change_language_iv:
                navigateTo(R.id.action_mainFragment_to_languageFragment);
                break;

            case R.id.search_image_view:
                toggleToolBar();
                break;

            case R.id.add_timer_layout:
            case R.id.no_timer_tv:
                navigateTo(R.id.action_mainFragment_to_addTimerFragment);
                break;

            case R.id.pause_running_timer_bu:
                boolean isSelect = !view.isSelected();
                view.setSelected(isSelect);
                if (isSelect) {
                    startService();
                    timerService.start();
                } else timerService.pause();
                break;

            case R.id.close_running_timer_iv:
                timerService.close();
                closeRunningTimerLayout();
                break;

            case R.id.running_timer_layout:
                if (timerService != null) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Timer.TIMER, timerService.getTimer());
                    Navigation.findNavController(binding.getRoot()).navigate(R.id.action_mainFragment_to_timerDetailsFragment, bundle);
                }
                break;
        }
    }

    public void startService() {
        if (timerService == null) {
            Intent intent = new Intent(activity, TimerService.class);
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

    public void toggleToolBar() {
        if (binding.searchCardView.getVisibility() == View.VISIBLE) {
            viewModel.setSearchText(null);
            binding.searchEditText.setText(null);
            hideKeyboard(binding.searchEditText);
            binding.searchCardView.setVisibility(View.GONE);
            binding.navbarItemLayout.setVisibility(View.VISIBLE);
        } else {
            viewModel.setSearchText(null);
            binding.searchEditText.setText(null);
            binding.navbarItemLayout.setVisibility(View.GONE);
            binding.searchCardView.setVisibility(View.VISIBLE);
            showKeyboard(binding.searchEditText);
        }
    }

    @Override
    public void onTimerClick(Timer timer) {
        viewModel.setSelectedTimer(timer);
        navigateTo(R.id.action_mainFragment_to_timerDetailsFragment);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (viewModel.getSearchText().getValue() != null) {
            showKeyboard(binding.searchEditText);
        }

        if (timerService == null && TimerService.timerService != null && TimerService.timerService.isStart()) {
            startService();
        }

        LocalBroadcastManager.getInstance(activity).registerReceiver(mainTimerBroadcastReceiver, new IntentFilter(TimerDetailsFragment.TimerBroadCastReceiver.KEY));
    }

    @Override
    public void onPause() {
        super.onPause();
        if (viewModel.getSearchText().getValue() != null)
            hideKeyboard(binding.searchEditText);

        if (serviceBounded)
            unBindService();

        LocalBroadcastManager.getInstance(activity).unregisterReceiver(mainTimerBroadcastReceiver);
    }

    private void closeRunningTimerLayout() {
        Animation slideUpAnimation = new TranslateAnimation(0, 0, 0, -100);

        slideUpAnimation.setDuration(200);
        slideUpAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                binding.runningTimerLayout.setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                binding.runningTimerLayout.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        binding.runningTimerLayout.startAnimation(slideUpAnimation);
    }

    @Override
    public void onFilterComplete(int i) {
        if (i == 0 && !lottieAnimationView.isAnimating()) {
            lottieAnimationView.setVisibility(View.VISIBLE);
            lottieAnimationView.playAnimation();
        } else if (i > 0) {
            lottieAnimationView.pauseAnimation();
            lottieAnimationView.setVisibility(View.GONE);
        }
    }
}