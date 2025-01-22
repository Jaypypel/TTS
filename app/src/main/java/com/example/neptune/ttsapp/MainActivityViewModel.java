//package com.example.neptune.ttsapp;
//
//import android.widget.AutoCompleteTextView;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ListView;
//import android.widget.Spinner;
//import android.widget.TextView;
//
//import androidx.appcompat.app.ActionBarDrawerToggle;
//import androidx.appcompat.widget.Toolbar;
//import androidx.drawerlayout.widget.DrawerLayout;
//import androidx.lifecycle.LiveData;
//import androidx.lifecycle.MutableLiveData;
//import androidx.lifecycle.ViewModel;
//
//import com.example.neptune.ttsapp.DTO.DailyTimeShareDTO;
//import com.example.neptune.ttsapp.Network.ActivityServiceInterface;
//import com.example.neptune.ttsapp.Network.DailyTimeShareInterface;
//import com.example.neptune.ttsapp.Network.ProjectServiceInterface;
//import com.example.neptune.ttsapp.Network.TaskServiceInterface;
//import com.example.neptune.ttsapp.Util.DateConverter;
//import com.example.neptune.ttsapp.repository.ActivityRepository;
//import com.example.neptune.ttsapp.repository.MeasurableRepository;
//import com.example.neptune.ttsapp.repository.ProjectRepository;
//import com.example.neptune.ttsapp.repository.TaskRepository;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import javax.inject.Inject;
//
//public class MainActivityViewModel extends ViewModel {
//
//    private final ProjectRepository projectRepository;
//    private final TaskRepository taskRepository;
//    private final ActivityRepository activityRepository;
//    private final MeasurableRepository measurableRepository;
//
//
//
//    private final MutableLiveData<String> date  = new MutableLiveData<>();
//    private final MutableLiveData<String>startTime = new MutableLiveData<>();
//    private final MutableLiveData<String> endTime = new MutableLiveData<>();
//    private  final MutableLiveData<String> description = new MutableLiveData<>();
//    private  final MutableLiveData<String> projectCode = new MutableLiveData<>();
//    private  final MutableLiveData<String>  activityName = new MutableLiveData<>();
//    private  final  MutableLiveData<String> taskName = new MutableLiveData<>();
//    private final MutableLiveData<String> projectName = new MutableLiveData<>();
//
//
//    private final MutableLiveData<String> measurableQty = new MutableLiveData<String>();
//    private final MutableLiveData<String> measurableUnit = new MutableLiveData<>();
//    private final LiveData<Resource<List<MeasurableListDataModel>>> measurableOptions;
//    private final MutableLiveData<String> selectedMeasurableOption =  new MutableLiveData<>() ;
//
//    private final MutableLiveData<String>  currentTime = new MutableLiveData<>(),currentDate = new MutableLiveData<>();
//    private final LiveData<Resource<ArrayList<String>>> projectNameSuggestions,activityNameSuggestions,
//            taskNameSuggestions;
//
//
//    @Inject
//    public MainActivityViewModel(ProjectRepository projectRepository, TaskRepository taskRepository, ActivityRepository activityRepository, MeasurableRepository measurableRepository) {
//        this.projectRepository = projectRepository;
//        this.taskRepository = taskRepository;
//        this.activityRepository = activityRepository;
//        this.measurableRepository = measurableRepository;
//        this.projectNameSuggestions = projectRepository.getProjectNames();
//        this.taskNameSuggestions = taskRepository.getTaskNames();
//        this.activityNameSuggestions = activityRepository.getActivityNames();
//        this.measurableOptions = measurableRepository.getMeasurables();
//        currentDate.setValue(DateConverter.currentDate());
//        currentTime.setValue(DateConverter.currentTime());
//        measurableOptions.observeForever(resource -> {
//            if (resource != null && resource.data != null && !resource.data.isEmpty()) {
//                selectedMeasurableOption.setValue(resource.data.get(0).getMeasurableName());
//            }
//        });
//    }
//
//
//
//
//    public boolean validateUserInputs(){
//
//    }
//
//
//    public LiveData<String> getDate() {
//        return date;
//    }
//
//    public LiveData<Resource<ArrayList<String>>> getProjectNameSuggestions() {
//        return projectNameSuggestions;
//    }
//
//    public LiveData<Resource<ArrayList<String>>> getActivityNameSuggestions() {
//        return activityNameSuggestions;
//    }
//
//    public LiveData<Resource<ArrayList<String>>> getTaskNameSuggestions() {
//        return taskNameSuggestions;
//    }
//
//    public LiveData<String> getStartTime() {
//        return startTime;
//    }
//
//    public LiveData<String> getEndTime() {
//        return endTime;
//    }
//
//    public LiveData<String> getDescription() {
//        return description;
//    }
//
//    public LiveData<String> getProjectCode() {
//        return projectCode;
//    }
//
//    public LiveData<String> getActivityName() {
//        return activityName;
//    }
//
//    public LiveData<String> getTaskName() {
//        return taskName;
//    }
//
//    public LiveData<String> getProjectName() {
//        return projectName;
//    }
//
//    public LiveData<String> getMeasurableQty() {
//        return measurableQty;
//    }
//
//    public LiveData<String> getMeasurableUnit() {
//        return measurableUnit;
//    }
//
//    public LiveData<String> getCurrentTime() {
//        return currentTime;
//    }
//
//    public void setDate(String newDate) {
//        date.setValue(newDate);
//    }
//
//    public void setStartTime(String newStartTime) {
//        startTime.setValue(newStartTime);
//    }
//
//    public void setEndTime(String newEndTime) {
//        endTime.setValue(newEndTime);
//    }
//
//    public void setDescription(String newDescription) {
//        description.setValue(newDescription);
//    }
//    public void setProjectCode(String newProjectCode) {
//        projectCode.setValue(newProjectCode);
//    }
//    public void setActivityName(String newActivityName) {
//        activityName.setValue(newActivityName);
//    }
//    public void setTaskName(String newTaskName) { taskName.setValue(newTaskName); }
//    public void setProjectName(String newProjectName) { projectName.setValue(newProjectName); }
//    public void setMeasurableQty(String newMeasurableQty) { measurableQty.setValue(newMeasurableQty); }
//    public void setMeasurableUnit(String newMeasurableUnit) { measurableUnit.setValue(newMeasurableUnit); }
//    public void setSelectedMeasurableOption(String option) { selectedMeasurableOption.setValue(option); }
//}
