package com.example.neptune.ttsapp.Network;


import com.example.neptune.ttsapp.DTO.DailyTimeShareDTO;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface DailyTimeShareInterface {
    @POST("/dailyTimeShares/dailyTimeShare/")
    Call<APIResponse<Object>> addDailyTimeShare(@Body DailyTimeShareDTO dailyTimeShareDTO);
}
