package com.android.mhs.fitimer.ui.main;

import android.annotation.SuppressLint;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Filter;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;


import com.android.mhs.fitimer.R;
import com.android.mhs.fitimer.databinding.TimerItemBinding;
import com.android.mhs.fitimer.model.Timer;
import com.android.mhs.fitimer.data.local.prefs.AppPreferencesHelper;
import com.android.mhs.fitimer.ui.base.BaseAdapter;
import com.android.mhs.fitimer.ui.base.BaseViewHolder;
import com.android.mhs.fitimer.ui.main.callbacks.OnTimerClick;
import com.android.mhs.fitimer.utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TimersAdapter extends BaseAdapter<Timer> {

    private final OnTimerClick onTimerClick;

    public TimersAdapter(OnTimerClick onTimerClick) {
        this.onTimerClick = onTimerClick;
    }

    @NonNull
    @Override
    public TimerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (layoutInflater == null) layoutInflater = LayoutInflater.from(parent.getContext());
        TimerItemBinding itemBinding = TimerItemBinding.inflate(layoutInflater, parent, false);
        return new TimerViewHolder(itemBinding);
    }

    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint == null || constraint.length() == 0) {
                    filterResults.count = items.size();
                    filterResults.values = items;
                } else {
                    List<Timer> result = new ArrayList<>();
                    String searchStr = CommonUtils.convertNumbers(constraint.toString());

                    for (Timer timer : items) {
                        if (CommonUtils.convertNumbers(timer.getTitle()).contains(searchStr)) {
                            result.add(timer);
                        }
                    }
                    filterResults.count = result.size();
                    filterResults.values = result;
                }
                return filterResults;
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filterItems = (List<Timer>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    private class TimerViewHolder extends BaseViewHolder<Timer, TimerItemBinding> {

        public TimerViewHolder(TimerItemBinding itemBinding) {
            super(itemBinding);
            itemBinding.timerTitleTv.setSelected(true);
        }

        @Override
        public void onBind(Timer timer) {
            itemBinding.setTimer(timer);
            itemBinding.setOnTimerClick(onTimerClick);
            if (Locale.getDefault().getLanguage().equals(AppPreferencesHelper.EN)) {
                itemBinding.timerTitleTv.setGravity(Gravity.LEFT);
            } else if (Locale.getDefault().getLanguage().equals(AppPreferencesHelper.FA)) {
                itemBinding.timerTitleTv.setGravity(Gravity.RIGHT);
            }
            setColor(getAdapterPosition());
            itemBinding.executePendingBindings();
        }

        private void setColor(int position) {
            int remind = position % 6;
            if (remind == 1) {
                itemBinding.lineView.setBackgroundTintList(ContextCompat.getColorStateList(itemView.getContext(), R.color.red_500));
            } else if (remind == 2) {
                itemBinding.lineView.setBackgroundTintList(ContextCompat.getColorStateList(itemView.getContext(), R.color.light_blue_200));
            } else if (remind == 3) {
                itemBinding.lineView.setBackgroundTintList(ContextCompat.getColorStateList(itemView.getContext(), R.color.purple_400));
            } else if (remind == 4) {
                itemBinding.lineView.setBackgroundTintList(ContextCompat.getColorStateList(itemView.getContext(), R.color.pink_400));
            } else if (remind == 5) {
                itemBinding.lineView.setBackgroundTintList(ContextCompat.getColorStateList(itemView.getContext(), R.color.orange_400));
            } else {
                itemBinding.lineView.setBackgroundTintList(ContextCompat.getColorStateList(itemView.getContext(), R.color.blue_600));
            }
        }
    }
}
