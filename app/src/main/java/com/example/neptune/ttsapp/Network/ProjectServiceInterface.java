package com.example.neptune.ttsapp.Network;

import android.app.UiAutomation;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ProjectServiceInterface {

    @GET("/Projects/name/list")
    Call<ResponseBody> getProjectNameList();

    @GET("/Projects/project/projectName")
    Call<ResponseBody> getProjectCodeViaProjectName(@Query("proj_name") String projectName);


    @GET("/Projects/list/code")
    Call<ResponseBody> getProjectCodesList();

    @POST("/Projects/project/")
    Call<ResponseBody> addProject(@Query("user_id") String username, @Query("activity_id")
    Long activityId, @Query("proj_code") String projectCode, @Query("proj_name") String prjNme
            , @Query("created_on") String createdOn);


}
