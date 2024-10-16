package com.example.neptune.ttsapp.Network;

import retrofit2.Call;
import retrofit2.http.GET;

public interface TaskServiceInterface {

    @GET("/tasks/list/name")
    Call<APIResponse<Object>> getTaskNames();
}
