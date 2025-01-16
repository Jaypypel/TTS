package com.example.neptune.ttsapp.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.neptune.ttsapp.AppExecutors;
import com.example.neptune.ttsapp.Network.TaskServiceInterface;
import com.example.neptune.ttsapp.Resource;

import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Response;

public class TaskRepository {
    private final TaskServiceInterface taskService;
    private final AppExecutors appExecutors;

    public TaskRepository(TaskServiceInterface taskService, AppExecutors appExecutors) {
        this.taskService = taskService;
        this.appExecutors = appExecutors;
    }

    public LiveData<Resource<ArrayList<String>>> getTaskNames(){
        MutableLiveData<Resource<ArrayList<String>>> result = new MutableLiveData<>();
        result.postValue(Resource.loading(null));
        appExecutors.getNetworkIO().execute(() -> {
            try{
                Response<ArrayList<String>> response = taskService.getTaskNames();
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
