package com.android.mhs.fitimer.ui.base;

import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public abstract class BaseAdapter<T> extends RecyclerView.Adapter<BaseViewHolder> {
    protected List<T> items;
    protected LayoutInflater layoutInflater;
    protected List<T> filterItems;

    protected BaseAdapter() {
    }

    protected BaseAdapter(List<T> items) {
        this.items = items;
        filterItems = this.items;
    }

    public void submitData(List<T> items) {
        this.items = items;
        filterItems = this.items;
    }

    @Override
    public int getItemCount() {
        return filterItems.size();
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.onBind(filterItems.get(position));
    }
}
