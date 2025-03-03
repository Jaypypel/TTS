package com.example.neptune.ttsapp.Network;

import com.example.neptune.ttsapp.DTO.TimeShareDTO;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface TimeShareServiceInterface {

    @GET("timeshares1/list/{taskId}")
    Call<ResponseBody> getTimeShares(@Path("taskId") Long taskId);

    @POST("timeshares1/timeshare")
    Call<ResponseBody> addTimeShare(@Body TimeShareDTO timeShare);
}
