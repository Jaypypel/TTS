package com.example.neptune.ttsapp.Network;

import com.example.neptune.ttsapp.DTO.TimeShareDTO;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface TimeShareServiceInterface {

    @GET("timeshares/list/{taskId}")
    Call<ResponseBody> getTimeShares(@Path("taskId") Long taskId);

    @GET("timeshares/timeshare")
    Call<ResponseBody> addTimeShare(@Body TimeShareDTO timeShare);
}
