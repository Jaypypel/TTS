package com.example.neptune.ttsapp.Network;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ProjectServiceInterface {

    @GET("Projects/name/list")
    Call<APIResponse<Object>> getProjectNameList();
}
