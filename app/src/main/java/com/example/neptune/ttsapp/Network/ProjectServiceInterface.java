package com.example.neptune.ttsapp.Network;

import android.app.UiAutomation;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ProjectServiceInterface {

    @GET("/Projects/name/list")
    Call<APIResponse<Object>> getProjectNameList();

    @GET("/Projects/project/projectName/")
    Call<APIResponse<Object>> getProjectCodeViaProjectName(@Query("proj_name") String projectName);
}
