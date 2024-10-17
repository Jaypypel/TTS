package com.example.neptune.ttsapp.Network;

import android.app.UiAutomation;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ProjectServiceInterface {

    @GET("Projects/name/list")
    Call<APIResponse<Object>> getProjectNameList();

    @GET("Projects/projectName/")
    Call<APIResponse<Object>> getProjectViaList(String projectCode);
}
