package com.example.neptune.ttsapp.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.neptune.ttsapp.AppExecutors;
import com.example.neptune.ttsapp.MeasurableListDataModel;
import com.example.neptune.ttsapp.Network.MeasurableServiceInterface;
import com.example.neptune.ttsapp.Resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

public class MeasurableRepository {
    private final AppExecutors appExecutors;
    private final MeasurableServiceInterface measurableService;

    public MeasurableRepository(AppExecutors appExecutors, MeasurableServiceInterface measurableService) {
        this.appExecutors = appExecutors;
        this.measurableService = measurableService;
    }

    public LiveData<Resource<List<MeasurableListDataModel>>> getMeasurables(){
        MutableLiveData<Resource<List<MeasurableListDataModel>>> result = new MutableLiveData<>();
        result.postValue(Resource.loading(null));
        appExecutors.getNetworkIO().execute(() -> {
            try{
                Response<List<MeasurableListDataModel>> response = measurableService.getMeasurableList();
                if(response.isSuccessful() && response.body() != null){
                    result.postValue(Resource.success(response.body()));
                }else {
                    result.postValue(Resource.error("Failed to fetch",null));

                }
            }catch (IOException e){
                result.postValue(Resource.error("Network error",null));
            }
        });
        return result;
    }

}
