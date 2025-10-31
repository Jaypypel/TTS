package com.example.neptune.ttsapp.Network;

import com.example.neptune.ttsapp.QueryResolutionRequest;

import com.example.neptune.ttsapp.RaiseAQueryRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface QueryServiceInterface {

    @POST("queries/query")
    Call<ResponseBody> raiseQuery(@Body RaiseAQueryRequest request);


    @POST("queries/query/resolve")
    Call<ResponseBody> resolveQuery(@Body QueryResolutionRequest request);



    @GET("queries/query")
    Call<ResponseBody> queriesAgainstTask(@Query("taskId") Long taskId );
}
