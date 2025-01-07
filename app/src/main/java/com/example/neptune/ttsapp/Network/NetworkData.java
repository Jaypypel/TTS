package com.example.neptune.ttsapp.Network;

import retrofit2.Response;

public interface NetworkData<T> {
    public T getData(Response<T> response);
    public boolean isLoading = true;
    public String getError(Exception e);
}
