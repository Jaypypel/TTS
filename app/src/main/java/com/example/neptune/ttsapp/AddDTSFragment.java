package com.example.neptune.ttsapp;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.ui.AppBarConfiguration;

import com.example.neptune.ttsapp.Network.APIErrorResponse;
import com.example.neptune.ttsapp.Network.APIResponse;
import com.example.neptune.ttsapp.Network.APISuccessResponse;
import com.example.neptune.ttsapp.Network.ActivityServiceInterface;
import com.example.neptune.ttsapp.Network.DTSMeasurableInterface;
import com.example.neptune.ttsapp.Network.DailyTimeShareInterface;
import com.example.neptune.ttsapp.Network.MeasurableServiceInterface;
import com.example.neptune.ttsapp.Network.ProjectServiceInterface;
import com.example.neptune.ttsapp.Network.ResponseBody;
import com.example.neptune.ttsapp.Network.TaskServiceInterface;
import com.example.neptune.ttsapp.Util.DateConverter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class AddDTSFragment extends Fragment{

    @Inject
    AppExecutors appExecutor;

    @Inject
    DailyTimeShareInterface dailyTimeShareInterface;

    @Inject
    ActivityServiceInterface activityService;

    @Inject
    ProjectServiceInterface projectService;

    @Inject
    TaskServiceInterface taskService;

    @Inject
    MeasurableServiceInterface measurableService;

    @Inject
    DTSMeasurableInterface dtsMeasurableInterface;


    private SessionManager sessionManager;


    private EditText timeShareDate,timeShareStartTime,timeShareEndTime,timeShareDescription,timeShareMeasurableQty;
    private TextView time,date,timeShareProjCode;
    private ListView listView;
    private AutoCompleteTextView timeShareActivityName,timeShareTaskName,timeShareProjName, timeShareMeasurableUnit;
    private MaterialButton timeShareCancel,timeShareSubmit,timeShareAddMeasurable;
    private Spinner timeShareMeasurable;
    private TextInputLayout tsDate,tsStartTime,tsEndTime,tsDescription,tsMeasurableQty,tsActivityName,
            tsTaskName,tsProjectName,tsMeasurableUnit;
    ArrayList<MeasurableListDataModel> measurableListDataModels = new ArrayList<>();
    
    private MeasurableListCustomAdapter measurableListCustomAdapter;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add_dts,container,false);
        

        
        //  mDrawerList =  findViewById(R.id.left_drawer);
        sessionManager = new SessionManager(requireContext());
        timeShareDate= view.findViewById(R.id.editTextMainDate);
        timeShareProjCode= view.findViewById(R.id.editTextMainProjNo);
        timeShareProjName= view.findViewById(R.id.editTextMainProjName);
        timeShareActivityName= view.findViewById(R.id.editTextMainActName);
        timeShareTaskName= view.findViewById(R.id.editTextMainTaskName);
        timeShareStartTime= view.findViewById(R.id.editTextMainStartTime);
        timeShareEndTime= view.findViewById(R.id.editTextMainEndTime);
        timeShareDescription= view.findViewById(R.id.editTextMainDescription);

       

        tsDate = view.findViewById(R.id.textInputMainDateLayout);
        tsStartTime = view.findViewById(R.id.textInputMainStartTimeLayout);
        tsEndTime = view.findViewById(R.id.textInputMainEndTimeLayout);
        tsDescription = view.findViewById(R.id.textInputMainDescriptionLayout);
        tsMeasurableQty = view.findViewById(R.id.textInputLayoutQty);
        tsActivityName = view.findViewById(R.id.textInputActivityLayout);
        tsProjectName= view.findViewById(R.id.textInputMainProjectLayout);
        tsTaskName = view.findViewById(R.id.textInputMainTaskNameLayout);
        tsMeasurableUnit = view.findViewById(R.id.textInputLayoutUnit);


        timeShareCancel= view.findViewById(R.id.buttonMainCancel);
        timeShareSubmit=view.findViewById(R.id.buttonMainSubmit);

        date = view.findViewById(R.id.textViewMainDate);
        time = view.findViewById(R.id.textViewMainTime);


        listView= view.findViewById(R.id.listMainMeasurable);
        timeShareAddMeasurable = view.findViewById(R.id.buttonMainMeasurableAdd);
        timeShareMeasurableQty = view.findViewById(R.id.editTextMainQty);
        timeShareMeasurableUnit = view.findViewById(R.id.editTextMainUnit);
        timeShareMeasurable =  view.findViewById(R.id.measurableSelect);
        
        appExecutor.getMainThread().execute(()-> {
            date.setText(DateConverter.currentDate());
            time.setText(DateConverter.currentTime());
        });

        // code for go to Next EditText when press button of DONE on keyboard

        timeShareTaskName.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE))
            { timeShareDescription.requestFocus(); }
            return false;
        });

        timeShareDescription.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE))
            { timeShareProjName.requestFocus(); }
            return false;
        });

        timeShareProjName.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE))
            { timeShareActivityName.requestFocus(); return true;}
            return false;
        });

        timeShareActivityName.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE))
            { timeShareMeasurable.requestFocus(); }
            return false;
        });

        timeShareMeasurableQty.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE))
            { timeShareMeasurableUnit.requestFocus(); }
            return false;
        });
        recreateMainView(view);

        timeShareMeasurable.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                timeShareMeasurable.performClick(); // Force open
            }
            return false;
        });


        timeShareAddMeasurable.setOnClickListener(v -> {

            try {
                timeShareMeasurable.getSelectedItem().toString();
                String tmeShrMsrble = timeShareMeasurable.getSelectedItem().toString();
                String tmeShreMsrbleQty = timeShareMeasurableQty.getText().toString();

                String tmeShreMsrblUnit = timeShareMeasurableUnit.getText().toString();

                String[]   parts = tmeShrMsrble.split("-");


                String numberPart = parts[0].split("\\.")[0]; // Cast to int to remove decimal

                // Extract the word part
                String wordPart = parts[1];

                if (tmeShreMsrbleQty.isEmpty()){
                    timeShareMeasurableQty.setError("Qty can't be blank");
                    return;
                }

                if (tmeShreMsrblUnit.isEmpty()){
                    timeShareMeasurableUnit.setError("Unit can't be blank");
                    return;
                }



                MeasurableListDataModel m = new MeasurableListDataModel();
                m.setId(numberPart);
                m.setMeasurableName(wordPart);
                m.setMeasurableQty(tmeShreMsrbleQty);
                m.setMeasurableUnit(tmeShreMsrblUnit);


                if(!measurableListDataModels.contains(m)){
                    measurableListDataModels.add(m);
                }else {
                    Snackbar snackbar = Snackbar.make(v, "Warning! measurable entry is already present", Snackbar.LENGTH_LONG);
                    View snackbarView = snackbar.getView();
                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) snackbarView.getLayoutParams();
                    params.gravity = Gravity.CENTER;
                    snackbarView.setLayoutParams(params);
                    snackbar.show();
                }
                measurableListCustomAdapter = new MeasurableListCustomAdapter(measurableListDataModels, getActivity());
                listView.setAdapter(measurableListCustomAdapter);
                clear();
            }
            catch (Exception e){e.printStackTrace();}

        });

        timeShareSubmit.setOnClickListener(v -> {

            try {
                timeShareSubmit.setEnabled(false);
                if (InternetConnectivity.isConnected())
                {

                    if(isProjectCodeValid().isEmpty()){
                        timeShareProjCode.setError("Project code need to be entered");

                        timeShareSubmit.setEnabled(true);
                        return;
                    }
                    if (isDateValid().isEmpty()) {
                        timeShareDate.setError("Date Cannot Be Empty");
                        timeShareSubmit.setEnabled(true);
                        return;
                    }
                    if (isStartTimeValid().equals(isEndTimeValid())){
                        Snackbar snackbar = Snackbar.make(v, "Warning! Start and End Time are the same", Snackbar.LENGTH_LONG);
                        View snackbarView = snackbar.getView();
                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) snackbarView.getLayoutParams();
                        params.gravity = Gravity.CENTER;
                        snackbarView.setLayoutParams(params);
                        snackbar.show();

                        timeShareSubmit.setEnabled(true);
                        return;

                    }
//                    if (Integer.valueOf(isStartTimeValid().split(":")[0]) < Integer.valueOf(isEndTimeValid().split(":")[0])){
//                        Snackbar.make(v, "Warning! Start time should be greater than End Time", Snackbar.LENGTH_LONG)
//                                .show();
//                        return;
//                    }
                    if (!(sessionManager.getToken().equals("Prerna") || sessionManager.getToken().equals("YoKo")) && !isDateValid().equals(getTodayDate()))
                    { Toast.makeText(getActivity(), "Date Has Been Expired Contact to Admin", Toast.LENGTH_LONG).show();

                        timeShareSubmit.setEnabled(true);
                        return;
                    }
                    if (isStartTimeValid().isEmpty()) { timeShareStartTime.setError("Start Time Cannot Be Empty");

                        timeShareSubmit.setEnabled(true);
                        return;
                    }
                    if (isEndTimeValid().isEmpty()) { timeShareEndTime.setError("End Time Cannot Be Empty");   timeShareSubmit.setEnabled(true);return; }
                    if (isTaskNameValid().isEmpty()) { timeShareTaskName.setError("Task Name Cannot Be Empty");  timeShareSubmit.setEnabled(true);return;}
                    if (isProjectNameValid().isEmpty()) { timeShareProjName.setError("Project Name Cannot Be Empty");  timeShareSubmit.setEnabled(true);return; }
                    if (isActivityNameValid().isEmpty()) { timeShareActivityName.setError("Activity Name Cannot Be Empty");  timeShareSubmit.setEnabled(true);return; }
                    if (getConsumedTime().contains("-")) { Toast.makeText(getActivity(), "Please Enter Valid End Time", Toast.LENGTH_LONG).show();   timeShareSubmit.setEnabled(false); return; }


                    if (measurableListDataModels != null && measurableListDataModels.isEmpty() || listView == null) {

                        Toast.makeText(getActivity(), "Measurable list is empty", Toast.LENGTH_LONG).show();
                        timeShareSubmit.setBackgroundResource(android.R.drawable.btn_default);
                        timeShareSubmit.setEnabled(true);
                        return;
                    }
                    timeShareSubmit.setBackgroundColor(Color.GRAY);

                    String projectcode = !isProjectNameValid().isEmpty()? isProjectCodeValid(): "No code received";
                    User user = new User(sessionManager.getToken());

                    DailyTimeShare dailyTimeShare = new DailyTimeShare(isDateValid(),projectcode
                            ,isProjectNameValid(),isActivityNameValid(),
                            isTaskNameValid(),isStartTimeValid(),isEndTimeValid(),getConsumedTime(),
                            isDescriptionValid(),delegationTime(),user );
                    appExecutor.getNetworkIO().execute(() -> addDailyTimeShare(dailyTimeShare).thenCompose(result -> {
                        Long id = Long.valueOf(result.get(1));
                        return addDailyTimeShareMeasurables(id,measurableListDataModels).thenAccept(finalResult -> {
                            if (finalResult) {
                                appExecutor.getMainThread().execute(() -> {
                                    Toast.makeText(requireContext(), "Time Share Inserted", Toast.LENGTH_LONG).show();
                                    clearAll();
                                    clear();
                                    timeShareSubmit.setBackgroundResource(android.R.drawable.btn_default);
                                    timeShareSubmit.setEnabled(true);
                                });
                            }
                        });
                    }).exceptionally(e -> {
                        appExecutor.getMainThread().execute(() -> {
                            timeShareSubmit.setBackgroundResource(android.R.drawable.btn_default);
                            Toast.makeText(getActivity(), "Insertion Failed", Toast.LENGTH_LONG).show();
                            timeShareSubmit.setEnabled(true);
                        });
                        return null;

                    }).join());


                } else {
                    Toast.makeText(getActivity(), "No Internet Connection", Toast.LENGTH_LONG).show();
                    timeShareSubmit.setVisibility(View.VISIBLE);
                    timeShareSubmit.setEnabled(true);

                }
            } catch (Exception e) { e.printStackTrace(); }
        });

        timeShareProjName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String projectName = timeShareProjName.getText().toString().trim();
                if (!projectName.isEmpty()) {
                    // Fetch the project code
                    getProjectCodeAndUpdateUi(projectName)
                            .thenAccept(projectCode -> appExecutor
                                    .getMainThread()
                                    .execute(() -> timeShareProjCode.setText(projectCode))
                    ).exceptionally(e -> {
                        appExecutor.getMainThread().execute(() -> Toast.makeText(getActivity(), "Failed to fetch project code due to ", Toast.LENGTH_LONG).show());
                        return null;
                    });
                } else {
                    // Handle case when project name is empty
                    timeShareProjName.setError("Project Name Cannot Be Empty");
                }
            }
        });

        timeShareCancel.setOnClickListener(v -> {clearAll();  timeShareSubmit.setVisibility(View.VISIBLE);  timeShareCancel.setVisibility(View.VISIBLE);});

        //  single click view Date and time pickers
        timeShareDate.setFocusable(false);
        timeShareStartTime.setFocusable(false);
        timeShareEndTime.setFocusable(false);


//
        timeShareDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker
                    .Builder
                    .datePicker()
                    .setTitleText("Select Date")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();

            if (!datePicker.isAdded()){
                datePicker.show(getChildFragmentManager(),"Date_Picker");
            }

            datePicker.addOnPositiveButtonClickListener(selection -> {
                Log.e("Date",""+datePicker.getHeaderText());
                appExecutor.getNetworkIO().execute(() -> {
                    LocalDate selectedDate = Instant
                            .ofEpochMilli(selection)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();

                    DateTimeFormatter df =  DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    String formattedDate = selectedDate.format(df);
                    appExecutor.getMainThread().execute(() -> timeShareDate.setText(formattedDate));
                });


            });
        });


        timeShareStartTime.setOnClickListener(v -> {
            MaterialTimePicker timePicker = new MaterialTimePicker
                    .Builder().setInputMode(MaterialTimePicker.INPUT_MODE_KEYBOARD)
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .setHour(12)
                    .setMinute(10)
                    .setTitleText("Select Start Time")
                    .build();
            if (!timePicker.isAdded()) timePicker
                    .show(getChildFragmentManager(),"Time_Picker");

            timePicker.addOnPositiveButtonClickListener(selection -> appExecutor.getNetworkIO().execute(() -> {
                int hour = timePicker.getHour();
                int minute = timePicker.getMinute();
                String amPm = (hour < 12) ? "AM" : "PM";
                int formattedHour = (hour == 0 || hour == 12) ? 12 : hour % 12;
                String formattedTime = String.format("%02d:%02d %s", formattedHour, minute, amPm);
                appExecutor.getMainThread().execute(()-> timeShareStartTime
                        .setText(formattedTime));
            }));
        });

        timeShareEndTime.setOnClickListener(v -> {
            MaterialTimePicker timePicker = new MaterialTimePicker
                    .Builder().setInputMode(MaterialTimePicker.INPUT_MODE_KEYBOARD)
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .setHour(12)
                    .setMinute(10)
                    .setTitleText("Select End Time")
                    .build();

            if(!timePicker.isAdded()){
                timePicker.show(getChildFragmentManager(),"Time_Picker");
            }
            timePicker.addOnPositiveButtonClickListener(selection -> {
                appExecutor.getNetworkIO().execute(() -> {
                    int hour = timePicker.getHour();
                    int minute = timePicker.getMinute();
                    String amPm = (hour < 12) ? "AM" : "PM";
                    int formattedHour = (hour == 0 || hour == 12) ? 12 : hour % 12;
                    String formattedTime = String.format("%02d:%02d %s", formattedHour, minute, amPm);
                    appExecutor.getMainThread().execute(() -> timeShareEndTime
                            .setText(formattedTime));
                });


            });

        });


        timeShareEndTime.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s)
            {
                try
                {
                    if (getConsumedTime() != null)
                    {
                        if (getConsumedTime().contains("-"))
                        {
                            Toast.makeText(getActivity(), "Please Enter Valid End Time", Toast.LENGTH_LONG).show();
                        }
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            public void onTextChanged(CharSequence s, int start, int before, int count) { }
        });

        return view;
    }

    private void updateMeasurableObjectsAdapter(List<MeasurableListDataModel> measurableObjects) {
        ArrayAdapter<MeasurableListDataModel> adapterMeasurable = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, measurableObjects);
        adapterMeasurable.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeShareMeasurable.setAdapter(adapterMeasurable);
    }

    private void updateProjectNamesAdapter(ArrayList<String> projectNames) {
        ArrayAdapter<String> projectNamesAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                projectNames);
        timeShareProjName.setAdapter(projectNamesAdapter);
        setupAutoCompleteTextView(timeShareProjName, projectNamesAdapter);

    }

    private void updateTaskNamesAdapter(ArrayList<String> taskNames) {
        ArrayAdapter<String> taskNamesAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                taskNames);
        timeShareTaskName.setAdapter(taskNamesAdapter);
        setupAutoCompleteTextView(timeShareTaskName, taskNamesAdapter);
    }

    private void updateActivityNamesAdapter(ArrayList<String> activityNames) {
        ArrayAdapter<String> activityNamesAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                activityNames);
        timeShareActivityName.setAdapter(activityNamesAdapter);
        setupAutoCompleteTextView(timeShareActivityName, activityNamesAdapter);

    }

    private void setupAutoCompleteTextView(AutoCompleteTextView textView, ArrayAdapter<String> adapter) {
        textView.setThreshold(1); // Start suggesting after 1 character
        textView.setDropDownBackgroundResource(android.R.color.white);
        textView.setDropDownVerticalOffset(4); // Add some spacing

        // Prevent dropdown from disappearing too quickly
        textView.setOnItemClickListener((parent, view, position, id) -> {
            String selection = (String) parent.getItemAtPosition(position);
            textView.setText(selection);
            textView.clearFocus();
        });

        // Handle text changes smoothly
        textView.addTextChangedListener(new TextWatcher() {
            private Handler handler = new Handler(Looper.getMainLooper());
            private Runnable filterRunnable;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (filterRunnable != null) {
                    handler.removeCallbacks(filterRunnable);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                filterRunnable = () -> adapter.getFilter().filter(s.toString());
                handler.postDelayed(filterRunnable, 300); // Delay filtering by 300ms
            }
        });
    }
    private void hideMainActivityViews() {
        timeShareMeasurable.setVisibility(View.GONE);
        timeShareDate.setVisibility(View.GONE);
        timeShareStartTime.setVisibility(View.GONE);
        timeShareEndTime.setVisibility(View.GONE);
        timeShareDescription.setVisibility(View.GONE);
        timeShareMeasurableQty.setVisibility(View.GONE);
        timeShareProjCode.setVisibility(View.GONE);
        timeShareActivityName.setVisibility(View.GONE);
        timeShareTaskName.setVisibility(View.GONE);
        timeShareProjName.setVisibility(View.GONE);
        timeShareMeasurableUnit.setVisibility(View.GONE);
        timeShareCancel.setVisibility(View.GONE);
        timeShareSubmit.setVisibility(View.GONE);
        timeShareAddMeasurable.setVisibility(View.GONE);
        listView.setVisibility(View.GONE);
        date.setVisibility(View.GONE);
        time.setVisibility(View.GONE);
        tsDate.setVisibility(View.GONE);
        tsStartTime.setVisibility(View.GONE);
        tsEndTime.setVisibility(View.GONE);
        tsDescription.setVisibility(View.GONE);
        tsMeasurableQty.setVisibility(View.GONE);
        tsActivityName.setVisibility(View.GONE);
        tsTaskName.setVisibility(View.GONE);
        tsMeasurableUnit.setVisibility(View.GONE);
        tsProjectName.setVisibility(View.GONE);
    }
    private void recreateMainView(View view) {
        // Make all fields visible again
        timeShareMeasurable.setVisibility(View.VISIBLE);
        timeShareDate.setVisibility(View.VISIBLE);
        timeShareStartTime.setVisibility(View.VISIBLE);
        timeShareEndTime.setVisibility(View.VISIBLE);
        timeShareDescription.setVisibility(View.VISIBLE);
        timeShareMeasurableQty.setVisibility(View.VISIBLE);
        timeShareProjCode.setVisibility(View.VISIBLE);
        timeShareActivityName.setVisibility(View.VISIBLE);
        timeShareTaskName.setVisibility(View.VISIBLE);
        timeShareProjName.setVisibility(View.VISIBLE);
        timeShareMeasurableUnit.setVisibility(View.VISIBLE);
        timeShareCancel.setVisibility(View.VISIBLE);
        timeShareSubmit.setVisibility(View.VISIBLE);
        timeShareAddMeasurable.setVisibility(View.VISIBLE);
        listView.setVisibility(View.VISIBLE);
        date.setVisibility(View.VISIBLE);
        time.setVisibility(View.VISIBLE);
        tsDate.setVisibility(View.VISIBLE);
        tsStartTime.setVisibility(View.VISIBLE);
        tsEndTime.setVisibility(View.VISIBLE);
        tsDescription.setVisibility(View.VISIBLE);
        tsMeasurableQty.setVisibility(View.VISIBLE);
        tsActivityName.setVisibility(View.VISIBLE);
        tsTaskName.setVisibility(View.VISIBLE);
        tsMeasurableUnit.setVisibility(View.VISIBLE);
        tsProjectName.setVisibility(View.VISIBLE);





        // Clear fields
        clearAll();

        // Reset date and time
        appExecutor.getMainThread().execute(() -> {
            date.setText(DateConverter.currentDate());
            time.setText(DateConverter.currentTime());
        });

        // Refresh adapters
        if (InternetConnectivity.isConnected()) {
            ProgressBar loadingIndicator = view.findViewById(R.id.progressBarInMainPage);
            loadingIndicator.setVisibility(View.VISIBLE);

            appExecutor.getNetworkIO().execute(() -> {
                CompletableFuture<ArrayList<String>> activityNames = getActivityNames();
                CompletableFuture<ArrayList<String>> projectNames = getProjectNames();
                CompletableFuture<ArrayList<String>> taskNames = getTaskNames();
                CompletableFuture<List<MeasurableListDataModel>> measurableObjects = getMeasurableListAndUpdateUi();
                CompletableFuture.allOf(activityNames, projectNames, taskNames, measurableObjects)
                        .thenRun(() -> appExecutor.getMainThread().execute(() -> {
                            updateActivityNamesAdapter(activityNames.join());
                            updateTaskNamesAdapter(taskNames.join());
                            updateProjectNamesAdapter(projectNames.join());
                            updateMeasurableObjectsAdapter(measurableObjects.join());
                            loadingIndicator.setVisibility(View.GONE);
                        }))
                        .exceptionally(e -> {
                            appExecutor.getMainThread().execute(() -> {
                                Toast.makeText(requireContext(),
                                        "Failed to refresh data due to "+e.getMessage(), Toast.LENGTH_LONG).show();
                                loadingIndicator.setVisibility(View.GONE);
                            });
                            return null;
                        });
            });
        }
    }

    private void clearAllFragments(){
        FragmentManager fragmentManager = getChildFragmentManager();
        for(Fragment fragment: fragmentManager.getFragments()){
            fragmentManager.beginTransaction().remove(fragment).commit();
        }
    }

    private void hideAllFragments(){
        FragmentManager fragmentManager = getChildFragmentManager();
        for (Fragment fragment : fragmentManager.getFragments()){
            fragmentManager.beginTransaction().hide(fragment).commit();
        }
    }

    // Add leading 0 when input date or time is single No like 5
    public String convertDateTime(int input)
    {
        if (input >= 10) { return String.valueOf(input); }
        else { return "0" + input; }
    }
    private String getTodayDate()
    {
        return DateConverter.currentDate();
    }

    //Validation Start
    private String isDateValid() {
        if(timeShareDate !=  null){
//            return timeShareDate.getText().toString().trim().replaceAll("\\s+","");
            return String.valueOf(timeShareDate.getText());
        }else
            return "time share date is not iniaitzed";
    }

    private String isActivityNameValid() {
        if(timeShareActivityName != null){
            return timeShareActivityName.getText().toString().trim();
        }
        return "not initaiztied";
    }

    private String isTaskNameValid() {
        if(timeShareTaskName != null){
            return timeShareTaskName.getText().toString().trim();
        }
        return "N.I";

    }

    private String isProjectCodeValid() { if (timeShareProjCode != null){
        return timeShareProjCode.getText().toString().trim();
    }else {
        return "N.I";
    }
    }

    private String isProjectNameValid() {
        if(timeShareProjName != null) return timeShareProjName.getText().toString().trim();
        else return "N.I";
    }

    private String isStartTimeValid() {
        if(timeShareStartTime !=null)return timeShareStartTime.getText().toString().trim();
        else return "N.I";
    }

    private String isEndTimeValid() {
        if(timeShareEndTime != null)return timeShareEndTime.getText().toString().trim();
        else return "N.I";
    }

    private String isDescriptionValid() {
        if(timeShareDescription  != null)return timeShareDescription.getText().toString().trim();
        else return "N.I";
    }
    //Validation End

    //Getting Current TimeStamp
    private String delegationTime()
    {
        return DateConverter.getCurrentDateTime();
    }


    // Clear All EditText
    public void clearAll()
    {
        timeShareDate.setText("");
        timeShareStartTime.setText("");
        timeShareEndTime.setText("");
        timeShareDescription.setText("");
        timeShareMeasurableQty.setText("");
        timeShareProjCode.setText("");
        timeShareActivityName.setText("");
        timeShareTaskName.setText("");
        timeShareProjName.setText("");
        measurableListDataModels.removeAll(measurableListDataModels);
        listView.setAdapter(new MeasurableListCustomAdapter(measurableListDataModels,requireContext()));
        timeShareMeasurableUnit.setText("");

    }

    // Clear the EditText of Measurable
    public void clear(){
        timeShareMeasurableQty.setText("");
        timeShareMeasurableUnit.setText("");
    }

    //Calculate Time Difference between startTime and endTime
    private String getConsumedTime() {
        String start= timeShareStartTime!=null ? timeShareStartTime.getText().toString().trim().replaceAll("\\s+","") : "N.I";
        String end=timeShareEndTime !=null ? timeShareEndTime.getText().toString().trim().replaceAll("\\s+","") : "N.I" ;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mma");
        LocalTime startTime = LocalTime.parse(start,formatter);
        LocalTime endTime = LocalTime.parse(end,formatter);
        long difference = ChronoUnit.MINUTES.between(startTime, endTime);
        int hours = (int) (difference/ 60);
        int mins = (int) (difference % 60);
        String timeConsumed = hours + " hr : "+mins+" mins";
        return timeConsumed;
    }


    private CompletableFuture<ArrayList<String>> addDailyTimeShare(DailyTimeShare dailyTimeShare) {
        CompletableFuture<ArrayList<String>> future = new CompletableFuture<>();
        //it is used for to message & dts Id from ResponseBody object
        ArrayList<String> messageAndId = new ArrayList<>();
        appExecutor.getNetworkIO().execute(() -> {
            Call<ResponseBody> call = dailyTimeShareInterface.addDailyTimeShare(dailyTimeShare);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        APIResponse apiResponse = APIResponse.create(response);
                        if (apiResponse != null) {
                            if (apiResponse instanceof APISuccessResponse) {
                                String message = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getMessage().getAsString();
                                JsonObject dtsobject = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getBody().getAsJsonObject();
                                String dtsId = dtsobject.get("id").getAsString();
                                if ("successful".equals(message)) {
                                    messageAndId.add(message);
                                    messageAndId.add(dtsId);
                                    future.complete(messageAndId);
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
                        future.completeExceptionally(new Throwable("Exception occured while getting assigned tasks due to" + e.getMessage()));
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

    public CompletableFuture<Boolean> addDailyTimeShareMeasurables(Long timeShareId,List<MeasurableListDataModel> measurableListDataModel){
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
                Call<ResponseBody> call = dtsMeasurableInterface.addDailyTimeShareMeasurable(timeShareId,measurableId, measurableQty,measurableUnit);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Log.d("Debug", "Response received for addDailyTimeShareMeasurables"+response.body());
                        APIResponse apiResponse = null;
                        try {
                            apiResponse = APIResponse.create(response);
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

                                } if (apiResponse instanceof APIErrorResponse) {
                                    String erMsg = ((APIErrorResponse<ResponseBody>) apiResponse).getErrorMessage();
                                    future.completeExceptionally(new Throwable(erMsg));

                                }
                                if (apiResponse instanceof APIErrorResponse) {
                                    future.completeExceptionally(new Throwable("empty response"));
                                }
                            }}
                        catch (ClassCastException e){
                            future.completeExceptionally(new Throwable("Unable to cast the response into required format due to "+ e.getMessage()));
                        }
                        catch (IOException e) {
                            Log.e("IOException", "Exception occurred: " + e.getMessage(), e);
                            future.completeExceptionally(new Throwable("Exception occured while getting assigned tasks due to" + e.getMessage()));
                        }
                        catch (RuntimeException e) {
                            future.completeExceptionally(new Throwable("Unnoticed Exception occurred which is "+ e.getMessage() +   " its cause "+e.getCause()));
                        }
                        finally {
                            if (pendingTasks.decrementAndGet() == 0){
                                future.complete(allSuccessful.get());
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e("IOException", "Exception occurred: " + t.getMessage(), t);
                        allSuccessful.set(false);
                        if (pendingTasks.decrementAndGet() == 0) {
                            future.completeExceptionally(new Throwable(t.getMessage()));
                        }
                    }
                });
            });
        }
        return future;
    }



    public CompletableFuture<ArrayList<String>> getProjectNames() {
        CompletableFuture<ArrayList<String>> future = new CompletableFuture();
//        AtomicArrayList<String> projectNames;
        Call<ResponseBody> call = projectService.getProjectNameList();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {


                try {
                    APIResponse apiResponse =   APIResponse.create(response);
                    if(apiResponse instanceof  APISuccessResponse){
                        JsonElement result = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getBody();
                        Gson gson = new Gson();
                        Type listType = new TypeToken<ArrayList<String>>() {}.getType();

                        if(result.isJsonArray()){
                            JsonArray projectNames = result.getAsJsonArray();
                            ArrayList<String> list = gson.fromJson(projectNames, listType);
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
                    future.completeExceptionally(new Throwable("Exception occured while getting assigned tasks due to" + e.getMessage()));
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


    public CompletableFuture<ArrayList<String>> getActivityNames() {
        CompletableFuture<ArrayList<String>> future = new CompletableFuture<>();
        Call<ResponseBody> call = activityService.getActivitiesName();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {


                try {
                    APIResponse apiResponse =   APIResponse.create(response);
                    if(apiResponse instanceof  APISuccessResponse){
                        JsonElement result = ((APISuccessResponse<ResponseBody>) apiResponse)
                                .getBody().getBody();
                        Gson gson = new Gson();
                        Type listType = new TypeToken<ArrayList<String>>() {}.getType();

                        if(result.isJsonArray()){
                            JsonArray activityNames = result.getAsJsonArray();
                            ArrayList<String> list = gson.fromJson(activityNames, listType);
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
                    future.completeExceptionally(new Throwable("Exception occured while getting assigned tasks due to" + e.getMessage()));
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


    public CompletableFuture<ArrayList<String>> getTaskNames() {
        CompletableFuture<ArrayList<String>> future = new CompletableFuture<>();
        Call<ResponseBody> call = taskService.getTaskNames();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {


                try {
                    APIResponse apiResponse =   APIResponse.create(response);
                    if(apiResponse instanceof  APISuccessResponse){
                        JsonElement result = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getBody();
                        Gson gson = new Gson();
                        Type listType = new TypeToken<ArrayList<String>>() {}.getType();

                        if(result.isJsonArray()){
                            JsonArray activityNames = result.getAsJsonArray();
                            ArrayList<String> list = gson.fromJson(activityNames, listType);
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
                    future.completeExceptionally(new Throwable("Exception occured while getting assigned tasks due to" + e.getMessage()));
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



    public CompletableFuture<String> getProjectCodeAndUpdateUi(String projectName) {
        CompletableFuture<String> future = new CompletableFuture<>();
        appExecutor.getNetworkIO().execute(() -> {

            Call<ResponseBody> projectCodeResponse = projectService.getProjectCodeViaProjectName(projectName);
            projectCodeResponse.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {


                    try {
                        APIResponse apiResponse = APIResponse.create(response);
                        if(apiResponse instanceof APISuccessResponse) {
                            JsonElement jsonElement = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getBody();
                            String projectCode = jsonElement != null ? jsonElement.getAsString(): "";

                            future.complete(projectCode);  Log.e("ProjectCode",projectCode);
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
                        future.completeExceptionally(new Throwable("Exception occured while getting assigned tasks due to" + e.getMessage()));
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
        } );
        return future;
    }



    public CompletableFuture<List<MeasurableListDataModel>> getMeasurableListAndUpdateUi() {
        CompletableFuture<List<MeasurableListDataModel>> future = new CompletableFuture<>();

        Call<ResponseBody> measurableListResponse = measurableService.getMeasurableList();
        measurableListResponse.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {


                try {

                    APIResponse  apiResponse = APIResponse.create(response);
                    if (apiResponse instanceof APISuccessResponse) {
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
                        String erMsg = ((APIErrorResponse<?>) apiResponse).getErrorMessage();
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
                    future.completeExceptionally(new Throwable("Exception occured while getting assigned tasks due to" + e.getMessage()));
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