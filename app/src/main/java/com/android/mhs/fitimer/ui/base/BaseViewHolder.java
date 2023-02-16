package com.android.mhs.fitimer.ui.base;

import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

public abstract class BaseViewHolder<T, V extends ViewDataBinding> extends RecyclerView.ViewHolder {

    protected V itemBinding;

    public BaseViewHolder(V itemBinding) {
        super(itemBinding.getRoot());
        this.itemBinding = itemBinding;
    }

    public abstract void onBind(T t);
}