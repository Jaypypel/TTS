package com.example.neptune.ttsapp.Network;


import com.example.neptune.ttsapp.DTO.DailyTimeShareDTO;
import com.example.neptune.ttsapp.DailyTimeShare;

import java.time.LocalDate;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface DailyTimeShareInterface {
    @POST("/dailyTimeShares/dailyTimeShare/")
    Call<ResponseBody> addDailyTimeShare(@Body DailyTimeShare dailyTimeShareDTO);

    @GET("/dailyTimeShares/dailyTimeShareList/{username}/{dateOfTimeShare}")
    Call<ResponseBody> getDailyTimeShareList(@Path("username") String username, @Path("dateOfTimeShare") String TSDate);
}
