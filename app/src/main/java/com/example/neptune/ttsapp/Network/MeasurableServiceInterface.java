package com.example.neptune.ttsapp.Network;

import com.example.neptune.ttsapp.MeasurableListDataModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MeasurableServiceInterface{


    @GET("Measurables1/list")
    Call<ResponseBody> getMeasurableList();


    @GET("Measurables1/measurable-list/")
    Call<ResponseBody> getMeasurables1();

    @GET("Measurables1/names")
    Call<ResponseBody> getMeasurableNamesbyUsername(@Query("username") String username);

    @GET("Measurables1/DTSMeasurables1List/{dtsId}")
    Call<ResponseBody> getDTSMeasurableList(@Path("dtsId") Long dtsId);

    @GET("/delegationMeasurables1/allocatedMeasurabeslist/{taskId}")
    Call<ResponseBody> getAllocatedMeasurableList(@Path("taskId") Long taskId);

    @POST("/delegationMeasurables1/add-delegationMeasurable")
    Call<ResponseBody> addDelegationMeasurable(@Query("taskHandlerId") Long taskHandlerId,
                                               @Query("measurablesId") Long measurablesId,
                                               @Query("mesrbQunty") Long mesrbQunty,
                                               @Query("mesrbUnit") String mesrbUnit);


    @POST("timesharemeasurables1/add/timeharemeasurable")
    Call<ResponseBody> addTimeShareMeasurable(@Query("timeShareId") Long timeShareId,
                                              @Query("measuableId")  Long measuableId,
                                              @Query("measurableQuantity") Long measurableQuantity,
                                              @Query("measurableUnit") String measurableUnit);



    @POST("Measurables1/measurable")
    Call<ResponseBody> addMeasurable(@Query("username") String username, @Query("measurableName") String measurableName, @Query("createdOn") String createdOn);
}
