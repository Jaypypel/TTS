package com.example.neptune.ttsapp.Network;


import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface DTSMeasurableInterface {
    @POST("/DailyTimeShareMeasurables/dailyTimeShareMeasurable")
     Call<ResponseBody> addDailyTimeShareMeasurable(@Query("timeShareId") Long timeShareId, @Query("measurablesId") Long measurablesId, @Query("mesrbQunty") Long mesrbQunty, @Query("mesrbUnit") String mesrbUnit);
}
