package com.example.neptune.ttsapp.Network;

import retrofit2.Call;
import retrofit2.http.GET;

public interface MeasurableServiceInterface {


    @GET("Measurables/list")
    Call<APIResponse<Object>> getMeasurableList();
}
