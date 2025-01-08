package com.example.neptune.ttsapp;


import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.neptune.ttsapp.Network.APIErrorResponse;
import com.example.neptune.ttsapp.Network.APIResponse;
import com.example.neptune.ttsapp.Network.APISuccessResponse;
import com.example.neptune.ttsapp.Network.MeasurableServiceInterface;
import com.example.neptune.ttsapp.Network.ResponseBody;
import com.example.neptune.ttsapp.Network.UserServiceInterface;
import com.example.neptune.ttsapp.Util.DateConverter;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Query;

@AndroidEntryPoint
public class TTSMeasurableCRUDFragment extends Fragment {

    @Inject
    AppExecutors appExecutors;

    @Inject
    UserServiceInterface userService;

    @Inject
    MeasurableServiceInterface measurableService;



    public TTSMeasurableCRUDFragment() { }

    private TextView user,date,time;
    private AutoCompleteTextView measurableName;
    private Button addMeasurable;

    private Spinner userSelect;

    private SessionManager sessionManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ttsmeasurable_crud, container, false);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        user=(TextView)view.findViewById(R.id.textViewMeasurableCRUDUser);
        sessionManager = new SessionManager(getActivity().getApplicationContext());
        user.setText(sessionManager.getUserID());

        date=(TextView)view.findViewById(R.id.textViewMeasurableCRUDDate);
        time=(TextView)view.findViewById(R.id.textViewMeasurableCRUDTime);


           appExecutors.getMainThread().execute(() -> {
            date.setText(DateConverter.currentDate());
            time.setText(DateConverter.currentTime());
        });


        measurableName=(AutoCompleteTextView) view.findViewById(R.id.editTextMeasurableCRUDMeasurable);

        userSelect=(Spinner) view.findViewById(R.id.spinnerMeasurableCRUDUserSelect);
        if (InternetConnectivity.isConnected()== true) {

            getUsernames().thenAccept(usernames -> {
                ArrayList<String>  users = usernames;
                users.add(0,"Select user");
                ArrayAdapter<String> userSelectAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item,users);
                userSelectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                userSelect.setAdapter(userSelectAdapter);
            }).exceptionally(e -> {Toast.makeText(getActivity().getApplicationContext(), "can't update usernames", Toast.LENGTH_LONG).show();
                return null;
            });
//            ArrayList users = getUserList();
//            users.add(0,"Select User");
//            ArrayAdapter<String> userSelectAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item,users);
//            userSelectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//            userSelect.setAdapter(userSelectAdapter);
        }else {Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();}

        try {
            userSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    // Set AutoCompleteTextView
                    if (InternetConnectivity.isConnected() == true) {
                            measurableName.setText("");
                        appExecutors.getNetworkIO().execute(() -> {
                            getMeasurableNames(getUser()).thenAccept(measurableNames -> {
                                ArrayAdapter<String> taskNameAdapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_list_item_1,measurableNames);
                                measurableName.setAdapter(taskNameAdapter);
                            }).exceptionally(e -> {
                                Toast.makeText(getActivity().getApplicationContext(), "can't update task names", Toast.LENGTH_LONG).show();
                                return null;
                            });
                        });
//                            ArrayAdapter<String> measurableNameAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, getMeasurableList(getUser()));
//                            measurableName.setAdapter(measurableNameAdapter);


                    } else {
                        Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) { }
            });
        }catch (Exception e){e.printStackTrace();}


        addMeasurable=(Button)view.findViewById(R.id.buttonMeasurableCRUDAddMeasurable);


        addMeasurable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try
                {
                    if (InternetConnectivity.isConnected()== true)
                    {
                     if (isMeasurableName().isEmpty()){measurableName.setError("Measurable Name Be Empty");
                        return;
                     }

                         addTask(getUser(), isMeasurableName(), createdOn()).thenAccept(isMeasurbaleAdded -> {
                             if(isMeasurbaleAdded.equals("successful")){
                                 appExecutors.getMainThread().execute(() ->
                                 {
                                     Toast.makeText(getActivity().getApplicationContext(), "Measurable Inserted ", Toast.LENGTH_LONG).show();
                                     measurableName.setText("");
                                 });
                             }else {
                                 appExecutors.getMainThread().execute(() -> Toast
                                         .makeText(getActivity()
                                                 .getApplicationContext(), "Insertion Failed", Toast.LENGTH_LONG)
                                         .show());
                             }
                         }).exceptionally(e -> {
                             Toast.makeText(getActivity().getApplicationContext(), "Failed to add activity due to "+e.getMessage(), Toast.LENGTH_LONG).show();

                             return null;
                         });

//                         String result = insertMeasurable(getUser(), isMeasurableName(), createdOn());
//                         if (result.equals("true")) {
//                             Toast.makeText(getActivity().getApplicationContext(), "Measurable Inserted ", Toast.LENGTH_LONG).show();
//                             measurableName.setText("");
//                         } else {
//                             Toast.makeText(getActivity().getApplicationContext(), "Insertion Failed", Toast.LENGTH_LONG).show();
//                         }

                    }else {Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();}


                }
                catch (Exception e){e.printStackTrace();}
            }
        });



        return view;
    }

    private String getUser()
    {
        String user = userSelect.getSelectedItem().toString().trim();
        return user;
    }

    private String isMeasurableName()
    {
        String actName = measurableName.getText().toString();
        if(actName.isEmpty()) { measurableName.setError("Measurable Name Be Empty"); }
        return actName;
    }

    private String createdOn()
    {
//        Calendar calendar = Calendar.getInstance();
//        Timestamp delegationTimestamp = new Timestamp(calendar.getTime().getTime());
//        return delegationTimestamp.toString();
        return DateConverter.getCurrentDateTime();
    }

    private CompletableFuture<String> addTask(String username, String measurableName,  String createdOn) {
        CompletableFuture<String> future = new CompletableFuture<>();
        //it is used for to message & dts Id from ResponseBody object
        Call<ResponseBody> call = measurableService.addMeasurable(username,measurableName,createdOn);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                APIResponse apiResponse = null;
                try {
                    apiResponse = APIResponse.create(response);
                    if (apiResponse != null) {
                        if (apiResponse instanceof APISuccessResponse) {
                            String message = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getMessage().getAsString();
                            // JsonObject dtsobject = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getBody().getAsJsonObject();

                            if ("successful".equals(message)) {
                                future.complete(message);
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
                        future.completeExceptionally(new Throwable("Exception occured while adding the measurable due to " + e.getMessage()));
                    }
                    catch (RuntimeException e) {
                        future.completeExceptionally(new Throwable("Unnoticed Exception occurred which is "+ e.getMessage() +   " its cause "+e.getCause()));
                    }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                future.completeExceptionally(new Throwable(t.getMessage()));
            }
        });
        return future;
    }

    public CompletableFuture<ArrayList<String>> getUsernames() {
        CompletableFuture<ArrayList<String>> future = new CompletableFuture<>();

        appExecutors.getNetworkIO().execute(() -> {
            Call<ResponseBody> usernamesResponse = userService.getUsernames();
            usernamesResponse.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {


                    try {
                        APIResponse apiResponse =   APIResponse.create(response);
                        if (apiResponse instanceof APISuccessResponse){
                            JsonElement result = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getBody();
                            Gson gson = new Gson();
                            Type listType = new TypeToken<ArrayList<String>>() {}.getType();

                            if(result.isJsonArray()){
                                JsonArray usernames = result.getAsJsonArray();
                                ArrayList<String> list = gson.fromJson(usernames, listType);
                                future.complete(list);

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
                    catch (ClassCastException e){
                        future.completeExceptionally(new Throwable("Unable to cast the response into required format due to "+ e.getMessage()));
                    }
                    catch (IOException e) {
                        Log.e("IOException", "Exception occurred: " + e.getMessage(), e);
                        future.completeExceptionally(new Throwable("Exception occured while getting  usernames due to " + e.getMessage()));
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

        });

        return future;
    }

    public CompletableFuture<ArrayList<String>> getMeasurableNames(String username) {
        CompletableFuture<ArrayList<String>> future = new CompletableFuture<>();

        appExecutors.getNetworkIO().execute(() -> {
            Call<ResponseBody> call = measurableService.getMeasurableNamesbyUsername(username);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {


                    try {
                        APIResponse apiResponse =   APIResponse.create(response);
                       if (apiResponse instanceof APISuccessResponse){
                           JsonElement result = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getBody();
                           Gson gson = new Gson();
                           Type listType = new TypeToken<ArrayList<String>>() {}.getType();

                           if(result.isJsonArray()){
                               JsonArray measurableNames = result.getAsJsonArray();
                               ArrayList<String> list = gson.fromJson(measurableNames, listType);
                               future.complete(list);
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
                    catch (ClassCastException e){
                    future.completeExceptionally(new Throwable("Unable to cast the response into required format due to "+ e.getMessage()));
                }
                    catch (IOException e) {
                    Log.e("IOException", "Exception occurred: " + e.getMessage(), e);
                    future.completeExceptionally(new Throwable("Exception occured while getting measurable names due to " + e.getMessage()));
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

        });

        return future;
    }
}
