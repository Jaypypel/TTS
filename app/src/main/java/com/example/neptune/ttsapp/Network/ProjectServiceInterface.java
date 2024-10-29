package com.example.neptune.ttsapp.Network;

import android.app.UiAutomation;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ProjectServiceInterface {

    @GET("/Projects/name/list")
    Call<List<String>> getProjectNameList();

    @GET("/Projects/project/projectName")
    Call<ResponseBody> getProjectCodeViaProjectName(@Query("proj_name") String projectName);
}
