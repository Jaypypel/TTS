package com.example.neptune.ttsapp.Network;

import java.time.LocalTime;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface TaskServiceInterface {

    @GET("/tasks1/list/name")
    Call<ResponseBody> getTaskNames();

    @GET("/tasks1/name")
    Call<ResponseBody> getTaskNamesByUsername(@Query("userId") String username);

    @POST("/tasks1/task")
    Call<ResponseBody>  addTask(@Query("username") String username,@Query("activityId") Long activityId,@Query("taskName") String taskName,@Query("createdOn") String createdOn);
}
