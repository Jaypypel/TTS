package com.example.neptune.ttsapp;

import java.io.Serializable;

public class ReportDTSDataModel implements Serializable
{

    private String timeShareDate;
    private String activityName;
    private String taskName;
    private String projectCode;
    private String projectName;
    private String startTime;
    private String endTime;
    private String consumedTime;
    private String measurableName;
    private String measurableQty;
    private String measurableUnit;
    private String description;


    public String getTimeShareDate() {
        return timeShareDate;
    }

    public void setTimeShareDate(String timeShareDate) {
        this.timeShareDate = timeShareDate;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getConsumedTime() {
        return consumedTime;
    }

    public void setConsumedTime(String consumedTime) {
        this.consumedTime = consumedTime;
    }

    public String getMeasurableName() {
        return measurableName;
    }

    public void setMeasurableName(String measurableName) {
        this.measurableName = measurableName;
    }

    public String getMeasurableQty() {
        return measurableQty;
    }

    public void setMeasurableQty(String measurableQty) {
        this.measurableQty = measurableQty;
    }

    public String getMeasurableUnit() {
        return measurableUnit;
    }

    public void setMeasurableUnit(String measurableUnit) {
        this.measurableUnit = measurableUnit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
