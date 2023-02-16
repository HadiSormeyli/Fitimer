package com.android.mhs.fitimer.ui.main;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.android.mhs.fitimer.BuildConfig;
import com.android.mhs.fitimer.R;
import com.android.mhs.fitimer.databinding.ActivityMainBinding;
import com.android.mhs.fitimer.ui.base.BaseActivity;
import com.android.mhs.fitimer.ui.base.BaseFragment;
import com.android.mhs.fitimer.viewmodel.MainViewModel;

import dagger.hilt.android.AndroidEntryPoint;
import ir.tapsell.plus.AdRequestCallback;
import ir.tapsell.plus.AdShowListener;
import ir.tapsell.plus.TapsellPlus;
import ir.tapsell.plus.TapsellPlusBannerType;
import ir.tapsell.plus.TapsellPlusInitListener;
import ir.tapsell.plus.model.AdNetworkError;
import ir.tapsell.plus.model.AdNetworks;
import ir.tapsell.plus.model.TapsellPlusAdModel;
import ir.tapsell.plus.model.TapsellPlusErrorModel;

@AndroidEntryPoint
public class MainActivity extends BaseActivity<ActivityMainBinding, MainViewModel> {

    private static final int ADDS_DELAY = 8000;
    private static String standardBannerResponseId;
    private NavHostFragment navHostFragment;
    private Handler handler;
    private Runnable runnable;
    private boolean destroyAdd = false;

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void preformViewModel() {
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel.setIsAddsShown(false);
        binding.destroyAddButton.setOnClickListener(v -> {
            binding.tapsellBannerView.setVisibility(View.GONE);
            destroyAdd();
        });

        final ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkRequest networkRequest = new NetworkRequest.Builder().build();
        connectivityManager.registerNetworkCallback(networkRequest, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                super.onAvailable(network);
                destroyAdd = false;
//                initAdds();
            }

            @Override
            public void onLost(Network network) {
                super.onLost(network);
                destroyAdd = true;
            }
        });

        navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
    }

    private void initAdds() {
        if (handler == null) {
            handler = new Handler(Looper.getMainLooper());
            runnable = new Runnable() {
                @Override
                public void run() {
                    if (!destroyAdd) {
                        showAdds();
                        handler.postDelayed(this, ADDS_DELAY);
                    } else {
                        handler.removeCallbacks(this);
                        handler = null;
                    }
                }
            };
        }

        TapsellPlus.initialize(this, BuildConfig.TAPSELL_KEY,
                new TapsellPlusInitListener() {
                    @Override
                    public void onInitializeSuccess(AdNetworks adNetworks) {
                        TapsellPlus.setGDPRConsent(MainActivity.this, true);
                        if (handler != null)
                            handler.postDelayed(runnable, 10);
                    }

                    @Override
                    public void onInitializeFailed(AdNetworks adNetworks,
                                                   AdNetworkError adNetworkError) {
                        destroyAdd = true;
                        viewModel.setIsAddsShown(false);
                        binding.tapsellBannerView.setVisibility(View.GONE);
                    }
                });
    }

    private void showAdds() {
        viewModel.setIsAddsShown(true);
        binding.tapsellBannerView.setVisibility(View.VISIBLE);
        TapsellPlus.requestStandardBannerAd(
                this, BuildConfig.ADDS_KEY,
                TapsellPlusBannerType.BANNER_320x50,
                new AdRequestCallback() {
                    @Override
                    public void response(TapsellPlusAdModel tapsellPlusAdModel) {
                        super.response(tapsellPlusAdModel);
                        if (isDestroyed()) return;
                        standardBannerResponseId = tapsellPlusAdModel.getResponseId();
                        TapsellPlus.showStandardBannerAd(MainActivity.this, standardBannerResponseId,
                                binding.tapsellBannerView,
                                new AdShowListener() {
                                    @Override
                                    public void onOpened(TapsellPlusAdModel tapsellPlusAdModel) {
                                        super.onOpened(tapsellPlusAdModel);
                                    }

                                    @Override
                                    public void onError(TapsellPlusErrorModel tapsellPlusErrorModel) {
                                        super.onError(tapsellPlusErrorModel);
                                    }
                                });
                    }

                    @Override
                    public void error(@NonNull String message) {
                        if (isDestroyed()) return;
                        destroyAdd = true;
                        binding.tapsellBannerView.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    public void onBackPressed() {
        BaseFragment fragment = (BaseFragment) navHostFragment.getChildFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (fragment != null) {
            if (fragment.isGuideViewShown()) {
                fragment.hideGuideView();
                return;
            }
        }
        super.onBackPressed();
    }

    private void destroyAdd() {
        viewModel.setIsAddsShown(false);
        destroyAdd = true;
        TapsellPlus.destroyStandardBanner(this, standardBannerResponseId, binding.tapsellBannerView);
    }

    @Override
    protected void onDestroy() {
        destroyAdd();
        super.onDestroy();
    }
}