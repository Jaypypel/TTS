package com.example.neptune.ttsapp;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.neptune.ttsapp.Network.APIEmptyResponse;
import com.example.neptune.ttsapp.Network.APIErrorResponse;
import com.example.neptune.ttsapp.Network.APIResponse;
import com.example.neptune.ttsapp.Network.APISuccessResponse;
import com.example.neptune.ttsapp.Network.DailyTimeShareInterface;
import com.example.neptune.ttsapp.Network.MeasurableServiceInterface;
import com.example.neptune.ttsapp.Network.ResponseBody;
import com.example.neptune.ttsapp.Util.DateConverter;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


@AndroidEntryPoint
public class TTSDailyTimeShareListFragment extends Fragment {


    @Inject
    AppExecutors appExecutor;

    @Inject
    DailyTimeShareInterface dailyTimeShareInterface;

    @Inject
    MeasurableServiceInterface measurableServiceInterface;

    public TTSDailyTimeShareListFragment() { }

    private SessionManager sessionManager;

    private ListView listViewDailyTimeShares;

    private TextView user,date,time;
    private TextView dailyTimeShares;

    private String userId;

    ArrayList<DailyTimeShareDataModel> dailyTimeShareDataList;

    private DailyTimeShareListCustomAdapter adapter;
    private Context mContext;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context.getApplicationContext();  // Use application context to avoid leaks
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_ttsdaily_time_share_list, container, false);
//        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//        StrictMode.setThreadPolicy(policy);

        listViewDailyTimeShares=(ListView)view.findViewById(R.id.listDailyTimeShare);

        sessionManager = new SessionManager(requireContext());
        userId = sessionManager.getToken();
        user=(TextView)view.findViewById(R.id.textViewDailyTimeShareListUser);
        user.setText(userId);

        date =(TextView)view.findViewById(R.id.textViewDailyTimeShareListDate);
        time =(TextView)view.findViewById(R.id.textViewDailyTimeShareListTime);
        dailyTimeShares = view.findViewById(R.id.dailyTimeShares);

        final Handler someHandler = new Handler(Looper.getMainLooper());
        someHandler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                Date date1 = new Date();
                String currentDate = formatter.format(date1);
                date.setText("Date :  " +currentDate);

                SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm a");
                Date time1 = new Date();
                String currentTime = timeFormatter.format(time1);
                time.setText("Time :  " +currentTime);
                someHandler.postDelayed(this, 1000);
            }
        }, 10);


       appExecutor.getNetworkIO().execute(() -> {
           getDailyTimeShareList(userId, getTodayDate()).thenAccept(result -> {
               dailyTimeShares.setVisibility(View.INVISIBLE);
               if (InternetConnectivity.isConnected()) {
                   appExecutor.getMainThread().execute(() -> {
                       dailyTimeShareDataList = result;
                       adapter = new DailyTimeShareListCustomAdapter(dailyTimeShareDataList, mContext);
                       listViewDailyTimeShares.setAdapter(adapter);
                       if(dailyTimeShareDataList == null ||dailyTimeShareDataList.isEmpty()){
                           dailyTimeShares.setVisibility(View.VISIBLE);
                       }
                   });
               } else {
                   Toast.makeText(mContext, "No Internet Connection", Toast.LENGTH_LONG).show();
               }
           }).exceptionally(e -> {
               Log.e("Error", "Failed to get DTSList due to" + e.getMessage());
               Toast.makeText(mContext, "Failed to get DTSList ", Toast.LENGTH_LONG).show();
               return null;
           });
       });


        if (InternetConnectivity.isConnected()) {
            listViewDailyTimeShares.setOnItemClickListener((parent, view1, position, id) -> {
                DailyTimeShareDataModel dataModel = dailyTimeShareDataList.get(position);
                appExecutor.getNetworkIO().execute(() -> {
                    getDTSMeasurableList(dataModel.getId()).thenAccept(result -> {
                        appExecutor.getMainThread().execute(() -> {
                            Intent i = new Intent(getActivity(),TTSTaskModificationActivity.class);
                            i.putExtra("DTSListItemDetails",dataModel);
                            i.putExtra("DTSMeasurableList",result);
                            startActivity(i);
                        });
                    }).exceptionally(e -> {
                        Log.e("Error", "c");
                        Toast.makeText(mContext,"Failed to get DTSMeasurableList",Toast.LENGTH_LONG).show();
                        return null;
                    });
                });

            });
        }


        return view;
    }

    private String getTodayDate()
    {
        return DateConverter.currentDate();
    }

    public CompletableFuture<ArrayList<DailyTimeShareDataModel>> getDailyTimeShareList(String username, String date){
        CompletableFuture<ArrayList<DailyTimeShareDataModel>> future = new CompletableFuture<>();

        Call<ResponseBody> call =  dailyTimeShareInterface.getDailyTimeShareList(username,date);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {


                try {
                    APIResponse apiResponse = APIResponse.create(response);
                    if(apiResponse != null){
                        if(apiResponse instanceof APISuccessResponse){
                            JsonElement bodyContent = ((APISuccessResponse<ResponseBody>) apiResponse)
                                    .getBody().getBody();
                            Gson gson = new Gson();
                            Type dailyTimeShareType = new TypeToken<ArrayList<DailyTimeShareDataModel>>(){}.getType();
                            if (bodyContent.isJsonArray()){
                                JsonArray content = bodyContent.getAsJsonArray();
                                ArrayList<DailyTimeShareDataModel> dailyTimeShareDataModels = gson.fromJson(content,dailyTimeShareType);
                                future.complete(dailyTimeShareDataModels);
                            }
                    }
                        if (apiResponse instanceof APIErrorResponse) {
                            String erMsg = ((APIErrorResponse<ResponseBody>) apiResponse).getErrorMessage();
                            future.completeExceptionally(new Throwable(erMsg));

                        }
                        if (apiResponse instanceof APIErrorResponse) {
                            future.completeExceptionally(new Throwable("empty response"));
                        }
                    }
                }
                catch (ClassCastException e){
                    future.completeExceptionally(new Throwable("Unable to cast the response into required format due to "+ e.getMessage()));
                }
                catch (IOException e) {
                    Log.e("IOException", "Exception occurred: " + e.getMessage(), e);
                    future.completeExceptionally(new Throwable("Exception occured while getting measurables due to" + e.getMessage()));
                }
                catch (RuntimeException e) {
                    future.completeExceptionally(new Throwable("Unnoticed Exception occurred which is "+ e.getMessage() +   " its cause "+e.getCause()));
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                future.completeExceptionally(new Throwable(t.getMessage()));
            }
        });
        return future;
    }

    public CompletableFuture<ArrayList<MeasurableListDataModel>> getDTSMeasurableList(Long dtsId){
        CompletableFuture<ArrayList<MeasurableListDataModel>> future = new CompletableFuture<>();

        Call<ResponseBody> call = measurableServiceInterface.getDTSMeasurableList(dtsId)   ;
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    APIResponse apiResponse = APIResponse.create(response);
                    if(apiResponse != null){
                        if(apiResponse instanceof APISuccessResponse){
                            JsonElement bodyContent = ((APISuccessResponse<ResponseBody>) apiResponse)
                                    .getBody().getBody();
                            Gson gson = new Gson();
                            Type measurableType = new TypeToken<ArrayList<MeasurableListDataModel>>(){}
                                    .getType();
                            if (bodyContent.isJsonArray()){
                                JsonArray content = bodyContent.getAsJsonArray();
                                ArrayList<MeasurableListDataModel> measurables = gson.fromJson(content,measurableType);
                                future.complete(measurables);
                            }
                        }
                        if (apiResponse instanceof APIErrorResponse) {
                            String erMsg = ((APIErrorResponse<ResponseBody>) apiResponse).getErrorMessage();
                            future.completeExceptionally(new Throwable(erMsg));

                        }
                        if (apiResponse instanceof APIErrorResponse) {
                            future.completeExceptionally(new Throwable("empty response"));
                        }
                    }
                }
                catch (ClassCastException e){
                        future.completeExceptionally(new Throwable("Unable to cast the response into required format due to "+ e.getMessage()));
                    }
                catch (IOException e) {
                        Log.e("IOException", "Exception occurred: " + e.getMessage(), e);
                        future.completeExceptionally(new Throwable("Exception occured while getting measurables due to" + e.getMessage()));
                    }
                catch (RuntimeException e) {
                        future.completeExceptionally(new Throwable("Unnoticed Exception occurred which is "+ e.getMessage() +   " its cause "+e.getCause()));
                    }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                future.completeExceptionally(new Throwable(t.getMessage()));
            }
        });
        return future;
    }



}
