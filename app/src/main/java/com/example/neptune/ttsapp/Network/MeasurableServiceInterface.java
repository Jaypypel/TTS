package com.example.neptune.ttsapp.Network;

import com.example.neptune.ttsapp.MeasurableListDataModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface MeasurableServiceInterface {


    @GET("Measurables/list")
    Call<List<MeasurableListDataModel>> getMeasurableList();
}
