package com.example.neptune.ttsapp.Network;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ReportServiceInterface {

    @GET("/report/dts")
    Call<ResponseBody> getDTSReportByUsernameAndDateRange(@Query("username") String username,
                                                          @Query("startDate") String startDate,
                                                          @Query("endDate") String endDate);
}