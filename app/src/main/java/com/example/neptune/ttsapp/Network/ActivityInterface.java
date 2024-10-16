package com.example.neptune.ttsapp.Network;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ActivityInterface {

    @GET("/activities/names")
    Call<APIResponse<Object>> getActivityList();
}
