package com.example.neptune.ttsapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import android.widget.Toast;

import com.example.neptune.ttsapp.DTO.TaskManagement;
import com.example.neptune.ttsapp.EnumStatus.Status;
import com.example.neptune.ttsapp.Network.APIErrorResponse;
import com.example.neptune.ttsapp.Network.APIResponse;
import com.example.neptune.ttsapp.Network.APISuccessResponse;
import com.example.neptune.ttsapp.Network.MeasurableServiceInterface;
import com.example.neptune.ttsapp.Network.ProjectServiceInterface;
import com.example.neptune.ttsapp.Network.ResponseBody;
import com.example.neptune.ttsapp.Network.TaskHandlerInterface;
import com.example.neptune.ttsapp.Util.DateConverter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class TTSTaskDelegationFragment extends Fragment {

    @Inject
    AppExecutors appExecutor;

    @Inject
    TaskHandlerInterface taskHandlerInterface;

    @Inject
    MeasurableServiceInterface measurableServiceInterface;

    @Inject
    ProjectServiceInterface projectServiceInterface;

    Status pending = Status.Pending;

    public TTSTaskDelegationFragment() { }

    EditText taskDeliExpDate,taskDeliExpTime, taskDeliTotalTimeHH,taskDeliTotalTimeMM,taskDeliDescription, taskDeliMeasurableQty;
    AutoCompleteTextView taskDeliActivityName,taskDeliTaskName,taskDeliProjName, taskDeliUserName, taskDeliMeasurableUnit;
    TextView taskDeliProjCode;
    Button taskDeleCancel,taskDelegate,addMeasurable;
    private TextView taskDeliUser,taskDeliDate,time;
    private Spinner spinnerMeasurable;

//    private ProgressBar progressBarInTaskDeli;

    private int mYear, mMonth, mDay, mHour, mMinute;

    private SessionManager sessionManager;

    ArrayList<MeasurableListDataModel> measurableListDataModels = new ArrayList<>();
    private ListView listView;
    private static MeasurableListCustomAdapter measurableListCustomAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_ttstask_delegation, container, false);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        sessionManager = new SessionManager(getActivity().getApplicationContext());

        taskDeliUser=view.findViewById(R.id.textViewTaskdeliUser);
        taskDeliUser.setText(sessionManager.getUserID());


        taskDeliDate=view.findViewById(R.id.textViewDate);
        time =  view.findViewById(R.id.textViewTime);

        final Handler someHandler = new Handler(Looper.getMainLooper());
        someHandler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                Date date1 = new Date();
                String currentDate = formatter.format(date1);
                taskDeliDate.setText("Date :  " +currentDate);

                SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm a");
                Date time1 = new Date();
                String currentTime = timeFormatter.format(time1);
                time.setText("Time :  " +currentTime);

                someHandler.postDelayed(this, 1000);
            }
        }, 10);



        taskDeliUserName = view.findViewById(R.id.editTextTaskDeliUserName);
        taskDeliActivityName= view.findViewById(R.id.editTextTaskDeliActName);
        taskDeliTaskName= view.findViewById(R.id.editTextTaskDeliTaskName);
        taskDeliProjCode= view.findViewById(R.id.editTextTaskDeliProjNo);
        taskDeliProjName= view.findViewById(R.id.editTextTaskDeliProjName);
        taskDeliExpDate= view.findViewById(R.id.editTextTaskDeliExpDate);
        taskDeliExpTime= view.findViewById(R.id.editTextTaskDeliExpTime);
        taskDeliTotalTimeHH = view.findViewById(R.id.editTextTaskDeliExpTotalTimeHH);
        taskDeliTotalTimeMM = view.findViewById(R.id.editTextTaskDeliExpTotalTimeMM);

        taskDeliDescription= view.findViewById(R.id.editTextTaskDeliDes);

        taskDeleCancel =view.findViewById(R.id.buttonTaskDeliCancel);
        taskDelegate=view.findViewById(R.id.buttonTaskDeligate);


        spinnerMeasurable =  view.findViewById(R.id.spinnerTaskDeliMeasurableSelect);

        // Code for Measurable list
        listView=view.findViewById(R.id.listTaskDeliMeasurable);
        addMeasurable=view.findViewById(R.id.buttontTaskDeliAdd);
        taskDeliMeasurableQty=view.findViewById(R.id.editTextTaskDeliQty);
        taskDeliMeasurableUnit = view.findViewById(R.id.editTextTaskDeliUnit);


        addMeasurable.setOnClickListener(v -> {
            try {
                String tmeShrMsrble =  spinnerMeasurable.getSelectedItem().toString() != null ? spinnerMeasurable.getSelectedItem().toString(): "undefined";
                String tmeShreMsrbleQty = taskDeliMeasurableQty.getText().toString();

                String tmeShreMsrblUnit = taskDeliMeasurableUnit.getText().toString();

                String[]   parts = tmeShrMsrble.split("-");
                Log.e("parts",""+parts);

                String numberPart = parts[0].split("\\.")[0]; // Cast to int to remove decimal
                Log.e("numberPart",""+numberPart);
                // Extract the word part
                String wordPart = parts[1];
                Log.e("wordPart",""+wordPart);

                MeasurableListDataModel m = new MeasurableListDataModel();
                m.setId(numberPart);
                m.setMeasurableName(wordPart);
                m.setMeasurableQty(tmeShreMsrbleQty);
                m.setMeasurableUnit(tmeShreMsrblUnit);

                measurableListDataModels.add(m);
                measurableListCustomAdapter = new MeasurableListCustomAdapter(measurableListDataModels, getActivity());
             listView.setAdapter(measurableListCustomAdapter);
                clearMeasurableDetails();

            }
            catch (Exception e){e.printStackTrace();}
        });



        taskDelegate.setOnClickListener(v -> {
            try {
                // Log when the button is clicked
                Log.d("TaskDelegate", "Button clicked");

                // Check internet connectivity
                if (!InternetConnectivity.isConnected()) {
                    Log.e("TaskDelegate", "No internet connection");
                    Toast.makeText(getActivity(), "No internet connection", Toast.LENGTH_SHORT).show();
                    return; // Exit early if no internet
                }

                // Validate UserName
                if (isReceivedUserValid().isEmpty()) {
                    taskDeliUserName.setError("UserName Cannot Be Empty");
                    return;
                }

                // Validate Task Name
                if (isTaskNameValid().isEmpty()) {
                    taskDeliTaskName.setError("Task Name Cannot Be Empty");
                    return;
                }

                // Validate Activity Name
                if (isActivityNameValid().isEmpty()) {
                    taskDeliActivityName.setError("Activity Name Cannot Be Empty");
                    return;
                }

                // Validate Project Name
                if (isProjectNameValid().isEmpty()) {
                    taskDeliProjName.setError("Project Name Cannot Be Empty");
                    return;
                }

                // Validate Expected Date
                if (isExpDateValid().isEmpty()) {
                    taskDeliExpDate.setError("Expected Date Cannot Be Empty");
                    return;
                }

                // Validate Expected Time
                if (isExpTimeValid().isEmpty()) {
                    taskDeliExpTime.setError("Expected Time Cannot Be Empty");
                    return;
                }

                // Validate Total Time (Minutes)
                String totalTime = taskDeliTotalTimeMM.getText().toString().trim().replaceAll("\\s+", "");
                if (totalTime.length() > 0) {
                    int totalTimeInt = Integer.parseInt(totalTime);
                    if (totalTimeInt > 60) {
                        Toast.makeText(getActivity(), "Invalid Minute", Toast.LENGTH_LONG).show();
                        return; // Exit if the time is invalid
                    }
                }

                // Log that inputs are valid and proceeding with task creation
                Log.d("TaskDelegate", "Valid inputs, proceeding with task creation");

                // Set background color to indicate the task is being processed
                taskDelegate.setBackgroundColor(Color.GRAY);
                String delegation = delegationTime();
                Log.e("delegation",""+delegation);
                // Create a new TaskManagement object anzzd set its values
                TaskManagement taskManagement = new TaskManagement();
                taskManagement.setActivityName(isActivityNameValid());
                taskManagement.setProjectCode(isProjectCodeValid());
                taskManagement.setProjectName(isProjectNameValid());
                taskManagement.setTaskName(isTaskNameValid());
                taskManagement.setDescription(isDescriptionValid());
                taskManagement.setTaskReceivedUserID(isReceivedUserValid());
                taskManagement.setTaskOwnerUserID(deligateOwnerUserId());
                taskManagement.setExpectedDate(isExpDateValid());
                taskManagement.setExpectedTime(isExpTimeValid());
                taskManagement.setActualTotalTime(isTotalTimeValid());
                taskManagement.setTaskAssignedOn(delegationTime());
                taskManagement.setStatus(pending.name());
                taskManagement.setTaskAcceptedOn("not_accepted");
                taskManagement.setTasKApprovedOn("not_approved");
                taskManagement.setTaskSeenOn("not_seen");
                taskManagement.setTaskCompletedOn("not_completed");
                taskManagement.setTaskProcessedOn("not_processed");


                                        appExecutor.getNetworkIO().execute(() -> {
                            assignTaskToUser(taskManagement).thenCompose(result -> {
                                Log.e("result",""+result);
                                Long id = Long.valueOf(result.get(1));
                                Log.e("id",""+id);

                                return addDailyTimeShareMeasurables(id,measurableListDataModels).thenAccept(finalResult -> {
                                    if (finalResult) {
                                        Log.e("finalResult",""+finalResult);

                                        appExecutor.getMainThread().execute(() -> {
                                            Toast.makeText(getActivity(), "Thank You..! Task Is Assigned", Toast.LENGTH_LONG).show();
                                            clearAll();
                                            clearMeasurableDetails();
                                            taskDelegate.setBackgroundResource(android.R.drawable.btn_default);
                                        });
                                    }
                                });
                            }).exceptionally(e -> {
                                appExecutor.getMainThread().execute(() -> {
                                    taskDelegate.setBackgroundResource(android.R.drawable.btn_default);
                                    Toast.makeText(getActivity(), "Task Delegation Failed", Toast.LENGTH_LONG).show();
                                });
                                return null;

                            }).join();
                        });

            } catch (Exception e) {
                Log.e("TaskDelegate", "Error in button click handler", e);
                Toast.makeText(getActivity(), "An error occurred while assigning the task", Toast.LENGTH_LONG).show();
            }
        });


        taskDeleCancel.setOnClickListener(v -> {

            clearAll();
            clearMeasurableDetails();
            taskDelegate.setBackgroundResource(android.R.drawable.btn_default);
        });




        try {

            if (InternetConnectivity.isConnected()) {


                clearAll();
                Toast.makeText(getActivity(), "Wait Loading Details", Toast.LENGTH_LONG).show();

                getMeasurableListAndUpdateUI().thenAccept(measurableListDataModels1 -> {
                   appExecutor.getMainThread().execute(() -> {
                        ArrayAdapter<MeasurableListDataModel> adapterMeasurable = new ArrayAdapter<MeasurableListDataModel>(getActivity(), android.R.layout.simple_spinner_item, measurableListDataModels1);
                        adapterMeasurable.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerMeasurable.setAdapter(adapterMeasurable);

                    });
                }).exceptionally(e ->{    Log.e("Error", "Failed to fetch measurable list: " + e.getMessage());
                    appExecutor.getMainThread().execute(() ->
                            Toast.makeText(getActivity(), "couldn't fetch measurable list", Toast.LENGTH_LONG).show());
                    return null;
                });


            } else { Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show(); }
;

        }catch (Exception e){e.printStackTrace();}




        //  single click view Date and time pickers
        taskDeliExpDate.setFocusable(false);
        taskDeliExpTime.setFocusable(false);

        //Date Picker for Expected Date start
        taskDeliExpDate.setOnClickListener((View v) -> {

            //To show current date in the DatePicker
            Calendar mcurrentDate=Calendar.getInstance();
            mYear=mcurrentDate.get(Calendar.YEAR);
            mMonth=mcurrentDate.get(Calendar.MONTH);
            mDay=mcurrentDate.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog mDatePicker=new DatePickerDialog(getActivity(), (view1, year, month, dayOfMonth) ->
                    taskDeliExpDate.setText(convertDateTime(dayOfMonth) + "-" + convertDateTime((month+1))  + "-" + year),mYear, mMonth, mDay);
            mDatePicker.getDatePicker().setCalendarViewShown(false);
            mDatePicker.setTitle("Select date");
            mDatePicker.show();

            // Get Project Code Against Project Name And Set To Project Code TextView
            String projectName = taskDeliProjName.getText().toString().trim();


            if (!projectName.isEmpty())
            {
                getProjectCodeAndUpdateUi(isProjectNameValid()).thenAccept(projectCode -> {
                    appExecutor.getMainThread().execute(() -> {
                        taskDeliProjCode.setText(projectCode);

                    });
                }).exceptionally(e -> {
                    Log.e("Project Code Error", "Failed to fetch project code: " + e.getMessage());
                    appExecutor.getMainThread().execute(() -> {
                        Toast.makeText(getActivity().getApplicationContext(), "Failed to fetch project code", Toast.LENGTH_LONG).show();
                    });
                    return null;
                });
            }else {
                taskDeliProjCode.setError("Project Name Cannot Be Empty");
            }
        });

        // Time Picker for Expected Time
        taskDeliExpTime.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            mHour = c.get(Calendar.HOUR_OF_DAY);
            mMinute = c.get(Calendar.MINUTE);

            // Launch Time Picker Dialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), (view12, hourOfDay, minute) ->
                    taskDeliExpTime.setText(convertDateTime(hourOfDay) + ":" + convertDateTime(minute)), mHour, mMinute, true);
            timePickerDialog.show();
        });


        taskDeliTotalTimeMM.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                String totalTimeMM= taskDeliTotalTimeMM.getText().toString().trim().replaceAll("\\s+","");
                if (totalTimeMM.length()>0)
                {
                    try
                    {
                        int min = Integer.parseInt(totalTimeMM);
                        if (min > 60)
                        {
                            Toast.makeText(getActivity().getApplicationContext(), "Invalid Minute", Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            public void onTextChanged(CharSequence s, int start, int before, int count) { }
        });



        // code for go to Next EditText when press button of DONE on keyboard

        taskDeliUserName.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE))
            { taskDeliTaskName.requestFocus(); }
            return false;
        });

        taskDeliTaskName.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE))
            { taskDeliDescription.requestFocus(); }
            return false;
        });

        taskDeliDescription.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE))
            { taskDeliActivityName.requestFocus(); }
            return false;
        });

        taskDeliActivityName.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE))
            { taskDeliProjName.requestFocus(); }
            return false;
        });

        taskDeliProjName.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE))
            { taskDeliTotalTimeHH.requestFocus(); }
            return false;
        });

        taskDeliTotalTimeHH.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE))
            { taskDeliTotalTimeMM.requestFocus(); }
            return false;
        });

        taskDeliTotalTimeMM.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE))
            { taskDeliMeasurableQty.requestFocus(); }
            return false;
        });

        taskDeliMeasurableQty.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE))
            { taskDeliMeasurableUnit.requestFocus(); }
            return false;
        });

        return view;
    }

    public void clearMeasurableDetails() {
        taskDeliMeasurableQty.setText("");
        taskDeliMeasurableUnit.setText("");
    }

    public void clearAll() {
        taskDeliUserName.setText("");
        taskDeliActivityName.setText("");
        taskDeliTaskName.setText("");
        taskDeliProjName.setText("");
        taskDeliProjCode.setText("");
        taskDeliDescription.setText("");
        taskDeliExpDate.setText("");
        taskDeliExpTime.setText("");
        taskDeliTotalTimeHH.setText("");
        taskDeliTotalTimeMM.setText("");
        listView.setAdapter(null);
    }

    // Add leading 0 when input date or time is single No like 5
    public String convertDateTime(int input) {
        if (input >= 10) { return String.valueOf(input); }
        else { return "0" + input; }
    }


    //Validation Start
    private String deligateOwnerUserId()
    {
        sessionManager = new SessionManager(getActivity().getApplicationContext());
        return sessionManager.getUserID();
    }

    private String isReceivedUserValid()
    {
        return taskDeliUserName.getText().toString().trim();
    }


    private String isActivityNameValid()
    {
        String actName = taskDeliActivityName.getText().toString().trim();
        if(actName.isEmpty()) { taskDeliActivityName.setError("Activity Name Cannot Be Empty"); }
        return actName;
    }


    private String isTaskNameValid()
    {
        String taskName = taskDeliTaskName.getText().toString().trim();
        if(taskName.isEmpty()) { taskDeliTaskName.setError("Task Name Cannot Be Empty"); }
        return taskName;
    }

    private String isProjectCodeValid()
    {
        return taskDeliProjCode.getText().toString().trim();
    }

    private String isProjectNameValid()
    {
        String projectName = taskDeliProjName.getText().toString().trim();
        if(projectName.isEmpty()) { taskDeliProjName.setError("Project Name Cannot Be Empty"); }
        return projectName;
    }

    private String isExpDateValid()
    {
        String expDate= taskDeliExpDate.getText().toString().trim().replaceAll("\\s+","");
        if(expDate.isEmpty()) { taskDeliExpDate.setError("Expected Date Cannot Be Empty"); }
        return expDate;
    }

    private String isExpTimeValid()
    {
        String expTime= taskDeliExpTime.getText().toString().trim().replaceAll("\\s+","");
        if(expTime.isEmpty()) { taskDeliExpTime.setError("Expected Time Cannot Be Empty"); }
        return expTime;
    }

    private String isTotalTimeValid()
    {
        String totalTimeHH= taskDeliTotalTimeHH.getText().toString().trim().replaceAll("\\s+","");
        String totalTimeMM= taskDeliTotalTimeMM.getText().toString().trim().replaceAll("\\s+","");

        if (totalTimeHH.isEmpty()) {totalTimeHH = "00";}
        if (totalTimeMM.isEmpty()) {totalTimeMM = "00";}

        return totalTimeHH + ":" + totalTimeMM;
    }

    private String isDescriptionValid()
    {

        return taskDeliDescription.getText().toString().trim();
    }

    private String delegationTime()
    {
//        Date currentDate = new Date();
//        SimpleDateFormat dateAndTimeFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm a");
//        dateAndTimeFormat.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
//        String ISTFormat = dateAndTimeFormat.format(currentDate);
//        return ISTFormat;
//        ZonedDateTime dateTimeInIST = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
//        DateTimeFormatter inOfPattern = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");
//        String currentDateAndTime = dateTimeInIST.format(inOfPattern);
//        return currentDateAndTime.trim();
        return  DateConverter.getCurrentDateTime();
    }


    public CompletableFuture<List<MeasurableListDataModel>> getMeasurableListAndUpdateUI() {
        CompletableFuture<List<MeasurableListDataModel>> future = new CompletableFuture<>();
        appExecutor.getNetworkIO().execute(() -> {
            Call<ResponseBody> measurableListResponse = measurableServiceInterface.getMeasurableList();
            measurableListResponse.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                    APIResponse apiResponse;
                    try {

                        apiResponse = APIResponse.create(response);
                        if (apiResponse instanceof APISuccessResponse) {
                            JsonElement responseElement =((APISuccessResponse<ResponseBody>) apiResponse).getBody().getBody();
                            if (responseElement.isJsonArray()) {
                                JsonArray responseBody = responseElement.getAsJsonArray();
                                ArrayList<MeasurableListDataModel> measurableListDataModels = new ArrayList<>();
                                for (JsonElement element : responseBody) {
                                    JsonObject obj = element.getAsJsonObject();
                                    double id = obj.get("id").getAsDouble();
                                    String measurableName = obj.get("measurableName").getAsString();
                                    MeasurableListDataModel m = new MeasurableListDataModel();
                                    m.setId(String.valueOf(id));
                                    m.setMeasurableName(measurableName);
                                    measurableListDataModels.add(m);
                                }
                                future.complete(measurableListDataModels);
                                Log.d("Meaurable List", "list" + measurableListDataModels);



                            }
                        }
                        if (apiResponse instanceof APIErrorResponse) {
                            String erMsg = ((APIErrorResponse<ResponseBody>) apiResponse).getErrorMessage();
                            Log.e("Error Received", "" + erMsg);
                        }
                        if (apiResponse instanceof APIErrorResponse) {
                            Log.e("Response", "is empty repsonse" + apiResponse);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }


                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e("Network Request", "Failed: " + t.getMessage()+"  "+t.getStackTrace());

                }
            });
        });
        return future;
    }

    public CompletableFuture<ArrayList<String>> assignTaskToUser(TaskManagement taskManagement){
        CompletableFuture<ArrayList<String>> future = new CompletableFuture<>();
        ArrayList<String> messageAndId = new ArrayList<>();
        Call<ResponseBody> call = taskHandlerInterface.addAssignTaskHandler(taskManagement);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                APIResponse apiResponse = null;
                try {
                    apiResponse = APIResponse.create(response);
                    if (apiResponse != null) {
                        if (apiResponse instanceof APISuccessResponse) {
                            String message = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getMessage().getAsString();
                            JsonObject dtsobject = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getBody().getAsJsonObject();
                            String dtsId = dtsobject.get("id").getAsString();
                            if ("Successful".equals(message)) {
                                messageAndId.add(message);
                                messageAndId.add(dtsId);
                                future.complete(messageAndId);
                            }
                        }
                    }
                } catch (IOException e) {
                    Log.e("IOException", "Exception occurred: " + e.getMessage(), e);
                    future.completeExceptionally(new Exception("API request failed: " + response.code()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                future.completeExceptionally(t);
            }
        });
        return future;
    }

    public CompletableFuture<Boolean> addDailyTimeShareMeasurables(Long taskHandlerId,List<MeasurableListDataModel> measurableListDataModel){
        CompletableFuture<Boolean> future  = new CompletableFuture<>();
        if(measurableListDataModel.isEmpty()) {
            future.complete(false);
            return future;
        }
        AtomicInteger pendingTasks = new AtomicInteger(measurableListDataModels.size());
        AtomicBoolean allSuccessful = new AtomicBoolean(true);
        for (MeasurableListDataModel m: measurableListDataModel
        ) {

            appExecutor.getNetworkIO().execute(() -> {
                Long measurableId = Long.valueOf(m.getId().split("\\.")[0]);
                Long measurableQty =  Long.valueOf(m.getMeasurableQty());
                String measurableUnit =  String.valueOf(m.getMeasurableUnit());
                Call<ResponseBody> call = measurableServiceInterface.addDelegationMeasurable(taskHandlerId,measurableId, measurableQty,measurableUnit);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                        Log.d("Debug", "Response received for addDailyTimeShareMeasurables"+response.body());

                        try {
                            APIResponse<ResponseBody> apiResponse = APIResponse.create(response);
                            if (apiResponse != null) {
                                if (apiResponse instanceof APISuccessResponse) {
                                    String message = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getMessage().getAsString();
                                    Log.d("Debug", "Message from API: " + message);
                                    if (!"successful".equals(message)) {
                                        Log.d("Debug", "Measurable completed with success");
                                        allSuccessful.set(false);
                                    }
                                }else {
                                    Log.e("Result", "apiresponse is nul");
                                    allSuccessful.set(false);

                                }
                            }
                        } catch (IOException e) {

                            Log.e("IOException", "Exception occurred: " + e.getMessage(), e);
                            allSuccessful.set(false);
                        }
                        finally {
                            if (pendingTasks.decrementAndGet() == 0){
                                future.complete(allSuccessful.get());
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                        Log.e("IOException", "Exception occurred: " + t.getMessage(), t);
                        allSuccessful.set(false);
                        if (pendingTasks.decrementAndGet() == 0) {
                            future.complete(false);
                        }
                    }
                });
            });
        }
        return future;
    }


    public CompletableFuture<String> getProjectCodeAndUpdateUi(String projectName) {
        CompletableFuture<String> future = new CompletableFuture<>();
        appExecutor.getNetworkIO().execute(() -> {

            Call<ResponseBody> projectCodeResponse = projectServiceInterface.getProjectCodeViaProjectName(projectName);
            projectCodeResponse.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {


                    try {
                        APIResponse<ResponseBody> apiResponse = APIResponse.create(response);
                        if(apiResponse instanceof APISuccessResponse) {
                            String projectCode = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getBody().getAsString();

                            if (projectCode != null){ future.complete(projectCode);  Log.e("ProjectCode",projectCode);}
                        }
                        if (apiResponse instanceof APIErrorResponse) {
                            String erMsg = ((APIErrorResponse<ResponseBody>) apiResponse).getErrorMessage();
                            Log.e("Error Received", erMsg);
                        }
                        if (apiResponse instanceof APIErrorResponse) {
                            Log.e("Response", "is empty repsonse" + apiResponse);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }


                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    future.completeExceptionally(t);

                }
            });
        } );
        return future;
    }



}