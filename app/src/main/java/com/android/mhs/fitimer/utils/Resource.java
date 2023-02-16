package com.android.mhs.fitimer.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Resource<T> {

    public final Status status;

    @Nullable
    public final T data;

    @Nullable
    public final String error;

    private Resource(Status status, @Nullable T data, @Nullable String error) {
        this.status = status;
        this.data = data;
        this.error = error;
    }

    public static <T> Resource<T> loading() {
        return new Resource<>(Status.LOADING, null, null);
    }

    public static <T> Resource<T> success(@NonNull T data) {
        return new Resource<>(Status.SUCCESS, data, null);
    }

    public static <T> Resource<T> error(@NonNull String error) {
        return new Resource<>(Status.ERROR, null, error);
    }
}