package com.example.neptune.ttsapp.Network;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ActivityServiceInterface {

    @GET("/activities/names")
    Call<List<String>> getActivitiesName();
}
