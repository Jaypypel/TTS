package com.example.neptune.ttsapp;

import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.neptune.ttsapp.DTO.DailyTimeShareDTO;

import java.util.ArrayList;

public class MainActivityViewModel extends ViewModel {
    public LiveData<String> getTimeShareMeasurableUnit() {
        return timeShareMeasurableUnit;
    }

    public void setTimeShareMeasurableUnit(String timeShareMeasurableUnit) {
        this.timeShareMeasurableUnit.setValue(timeShareMeasurableUnit);
    }

    public LiveData<String> getTimeShareProjName() {
        return timeShareProjName;
    }

    public void setTimeShareProjName(String timeShareProjName) {
        this.timeShareProjName.setValue(timeShareProjName);
    }

    public LiveData<String> getTimeShareTaskName() {
        return timeShareTaskName;
    }

    public void setTimeShareTaskName(String timeShareTaskName) {
        this.timeShareTaskName.setValue(timeShareTaskName);
    }

    public LiveData<String> getTimeShareActivityName() {
        return timeShareActivityName;
    }

    public void setTimeShareActivityName(String timeShareActivityName) {
        this.timeShareActivityName.setValue(timeShareActivityName);
    }

    public LiveData<String> getTimeShareProjCode() {
        return timeShareProjCode;
    }

    public void setTimeShareProjCode(String timeShareProjCode) {
        this.timeShareProjCode.setValue(timeShareProjCode);
    }

    public LiveData<String> getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date.setValue(date);
    }

    public LiveData<String> getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time.setValue(time);
    }

    public LiveData<String> getTimeShareMeasurableQty() {
        return timeShareMeasurableQty;
    }

    public void setTimeShareMeasurableQty(String timeShareMeasurableQty) {
        this.timeShareMeasurableQty.setValue(timeShareMeasurableQty);
    }

    public LiveData<String> getTimeShareDescription() {
        return timeShareDescription;
    }

    public void setTimeShareDescription(String timeShareDescription) {
        this.timeShareDescription.setValue(timeShareDescription);
    }

    public LiveData<String> getTimeShareEndTime() {
        return timeShareEndTime;
    }

    public void setTimeShareEndTime(String timeShareEndTime) {
        this.timeShareEndTime.setValue(timeShareEndTime);
    }

    public LiveData<String> getTimeShareStartTime() {
        return timeShareStartTime;
    }

    public void setTimeShareStartTime(String timeShareStartTime) {
        this.timeShareStartTime.setValue(timeShareStartTime);
    }

    public LiveData<String> getTimeShareDate() {
        return timeShareDate;
    }

    public void setTimeShareDate(String timeShareDate) {
        this.timeShareDate.setValue(timeShareDate);
    }

    //    DailyTimeShareDTO dailyTimeShareDTO;
//    private String[] mNavigationDrawerItemTitles;
//    private DrawerLayout mDrawerLayout;
//    private ListView mDrawerList;
//    Toolbar toolbar;
//    private CharSequence mDrawerTitle;
//    private CharSequence mTitle;
//    ActionBarDrawerToggle mDrawerToggle;
//    private SessionManager sessionManager;
    private MutableLiveData<String> timeShareDate,timeShareStartTime,timeShareEndTime,timeShareDescription,timeShareMeasurableQty;
    private MutableLiveData<String>  time,date,timeShareProjCode;
    private MutableLiveData<String>  timeShareActivityName,timeShareTaskName,timeShareProjName, timeShareMeasurableUnit;
    private Button timeShareCancel,timeShareSubmit,timeShareAddMeasurable;
    private Spinner timeShareMeasurable;
    //ArrayList<MeasurableListDataModel> measurableListDataModels = new ArrayList<>();
//    private ListView listView;
//    private MeasurableListCustomAdapter measurableListCustomAdapter;
    // private int mYear, mMonth, mDay, mHour, mMinute;
    // Code for Finishing activity from TimeShareList Activity
    //public static TTSMainActivity mainActivity;
}
