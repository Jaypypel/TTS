package com.example.neptune.ttsapp;

import java.io.Serializable;

public class DailyTimeShareDataModel implements Serializable
{
    Long timeShareId;
    String timeShareDate;
    String projectNo;
    String projectName;
    String activityName;
    String taskName;
    String startTime;
    String endTime;
    String consumedTime;
    String taskDescription;

    public Long getTimeShareId() {
        return timeShareId;
    }

    public void setTimeShareId(Long timeShareId) {
        this.timeShareId = timeShareId;
    }

    public String getTimeShareDate() {
        return timeShareDate;
    }

    public void setTimeShareDate(String timeShareDate) {
        this.timeShareDate = timeShareDate;
    }

    public String getProjectNo() {
        return projectNo;
    }

    public void setProjectNo(String projectNo) {
        this.projectNo = projectNo;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
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

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }
}
