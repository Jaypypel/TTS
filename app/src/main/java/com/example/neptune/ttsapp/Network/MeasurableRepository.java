package com.example.neptune.ttsapp.Network;

import androidx.annotation.NonNull;

import com.example.neptune.ttsapp.MeasurableListDataModel;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MeasurableRepository<T> implements NetworkData<T>{

    @Inject
    MeasurableServiceInterface measurableServiceInterface;



//    public List<MeasurableListDataModel> getMeasurables(){
//        measurableServiceInterface.getMeasurableList().enqueue(new Callback<APIResponse<T>>() {
//            @Override
//            public void onResponse(Call<APIResponse<T>> call, Response<APIResponse<T>> response) {
//                try {
//                    APIResponse apiResponse = APIResponse.create(response);
//                    if (apiResponse instanceof APISuccessResponse){
//                        Object content =  ((APISuccessResponse<?>) apiResponse).getBody();
//
//                    }
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//
//            @Override
//            public void onFailure(Call<APIResponse<T>> call, Throwable t) {
//
//            }
//
//        });
//        return java.util.Collections.emptyList();
//    }

    @Override
    public T getData(Response<T> response) {
        if(response.isSuccessful() && response.body()!=null){
            T body = response.body();
        }
        return null;
    }

    @Override
    public String getError(Exception e) {
        return "";
    }
}
