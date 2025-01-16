package com.example.neptune.ttsapp.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.neptune.ttsapp.AppExecutors;
import com.example.neptune.ttsapp.Network.ActivityServiceInterface;
import com.example.neptune.ttsapp.Resource;

import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Response;

public class ActivityRepository {
    private final AppExecutors appExecutors;
    private final ActivityServiceInterface activityService;

    public ActivityRepository(AppExecutors appExecutors, ActivityServiceInterface activityService) {
        this.appExecutors = appExecutors;
        this.activityService = activityService;
    }

    public LiveData<Resource<ArrayList<String>>> getActivityNames(){
        MutableLiveData<Resource<ArrayList<String>>> result = new MutableLiveData<>();
        result.postValue(Resource.loading(null));
        appExecutors.getNetworkIO().execute(() -> {
            try{
                Response<ArrayList<String>> response = activityService.getActivitiesName();
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
