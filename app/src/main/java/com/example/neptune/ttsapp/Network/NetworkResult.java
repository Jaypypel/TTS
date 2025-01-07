package com.example.neptune.ttsapp.Network;

public class NetworkResult<T> {
    public T data;

    public NetworkResult(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "NetworkResult{" +
                "data=" + data +
                '}';
    }
}
