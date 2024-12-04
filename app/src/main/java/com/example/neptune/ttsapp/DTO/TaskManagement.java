package com.example.neptune.ttsapp.DTO;





import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;



public class TaskManagement {


    public TaskManagement() {
    }

    private   String taskOwnerUserID;//check

    private   String taskReceivedUserID;//check

    private  String activityName;



    private String taskName;


    private String projectCode;

    private  String projectName;


    private String expectedDate;
    //added new expectedTime

    private String expectedTime;


    private String expectedTotalTime;


    private String description;

    @JsonFormat(pattern = "dd-MM-yyyy hh:mm a")
    private String taskAssignedOn;


    private String  actualTotalTime;

    private String taskSeenOn;


    private String taskCompletedOn;


    private String taskAcceptedOn;


    private String taskProcessedOn;


    private String tasKApprovedOn;


    private String status;


    public String getTaskOwnerUserID() {
        return taskOwnerUserID;
    }

    public void setTaskOwnerUserID(String taskOwnerUserID) {
        this.taskOwnerUserID = taskOwnerUserID;
    }

    public String getTaskReceivedUserID() {
        return taskReceivedUserID;
    }

    public void setTaskReceivedUserID(String taskReceivedUserID) {
        this.taskReceivedUserID = taskReceivedUserID;
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

    public String getExpectedDate() {
        return expectedDate;
    }

    public void setExpectedDate(String expectedDate) {
        this.expectedDate = expectedDate;
    }

    public String getExpectedTime() {
        return expectedTime;
    }

    public void setExpectedTime(String expectedTime) {
        this.expectedTime = expectedTime;
    }

    public String getExpectedTotalTime() {
        return expectedTotalTime;
    }

    public void setExpectedTotalTime(String expectedTotalTime) {
        this.expectedTotalTime = expectedTotalTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTaskAssignedOn() {
        return taskAssignedOn;
    }

    public void setTaskAssignedOn(String taskAssignedOn) {
        this.taskAssignedOn = taskAssignedOn;
    }

    public String getActualTotalTime() {
        return actualTotalTime;
    }

    public void setActualTotalTime(String actualTotalTime) {
        this.actualTotalTime = actualTotalTime;
    }

    public String getTaskSeenOn() {
        return taskSeenOn;
    }

    public void setTaskSeenOn(String taskSeenOn) {
        this.taskSeenOn = taskSeenOn;
    }

    public String getTaskCompletedOn() {
        return taskCompletedOn;
    }

    public void setTaskCompletedOn(String taskCompletedOn) {
        this.taskCompletedOn = taskCompletedOn;
    }

    public String getTaskAcceptedOn() {
        return taskAcceptedOn;
    }

    public void setTaskAcceptedOn(String taskAcceptedOn) {
        this.taskAcceptedOn = taskAcceptedOn;
    }

    public String getTaskProcessedOn() {
        return taskProcessedOn;
    }

    public void setTaskProcessedOn(String taskProcessedOn) {
        this.taskProcessedOn = taskProcessedOn;
    }

    public String getTasKApprovedOn() {
        return tasKApprovedOn;
    }

    public void setTasKApprovedOn(String tasKApprovedOn) {
        this.tasKApprovedOn = tasKApprovedOn;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


}
