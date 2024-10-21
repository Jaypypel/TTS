package com.example.neptune.ttsapp.Network;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface TaskServiceInterface {

    @GET("/tasks/list/name")
    Call<List<String>> getTaskNames();
}
