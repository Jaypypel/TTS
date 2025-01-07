package com.example.neptune.ttsapp;


import java.io.Serializable;

public class TaskDataModel implements Serializable {

    Long id;
    String taskOwnerUserID;
    String taskReceivedUserID;
    String activityName;
    String taskName;
    String projectCode;
    String projectName;
    String expectedDate;
    String expectedTotalTime;
    String description;
    String taskAssignedOn;
    String actualTotalTime;
    String taskSeenOn;
    String taskCompletedOn;
    String taskAcceptedOn;
    String taskProcessedOn;
    String tasKApprovedOn;
    String status;
    String modificationdescription;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getExpectedDate() {
        return expectedDate;
    }

    public void setExpectedDate(String expectedDate) {
        this.expectedDate = expectedDate;
    }

    public String getExpectedTotalTime() {
        return expectedTotalTime;
    }

    public void setExpectedTotalTime(String expectedTotalTime) { this.expectedTotalTime = expectedTotalTime; }

    public String getActualTotalTime() {
        return actualTotalTime;
    }

    public void setActualTotalTime(String actualTotalTime) { this.actualTotalTime = actualTotalTime; }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getModificationdescription() {
        return modificationdescription;
    }

    public void setModificationdescription(String modificationdescription) {
        this.modificationdescription = modificationdescription;
    }

    @Override
    public String toString() {
        return "TaskDataModel{" +
                "id=" + id +
                ", taskOwnerUserID='" + taskOwnerUserID + '\'' +
                ", taskReceivedUserID='" + taskReceivedUserID + '\'' +
                ", activityName='" + activityName + '\'' +
                ", taskName='" + taskName + '\'' +
                ", projectCode='" + projectCode + '\'' +
                ", projectName='" + projectName + '\'' +
                ", expectedDate='" + expectedDate + '\'' +
                ", expectedTotalTime='" + expectedTotalTime + '\'' +
                ", description='" + description + '\'' +
                ", taskAssignedOn='" + taskAssignedOn + '\'' +
                ", actualTotalTime='" + actualTotalTime + '\'' +
                ", taskSeenOn='" + taskSeenOn + '\'' +
                ", taskCompletedOn='" + taskCompletedOn + '\'' +
                ", taskAcceptedOn='" + taskAcceptedOn + '\'' +
                ", taskProcessedOn='" + taskProcessedOn + '\'' +
                ", tasKApprovedOn='" + tasKApprovedOn + '\'' +
                ", status='" + status + '\'' +
                ", modificationdescription='" + modificationdescription + '\'' +
                '}';
    }

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

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public String getTaskAssignedOn() {
        return taskAssignedOn;
    }

    public void setTaskAssignedOn(String taskAssignedOn) {
        this.taskAssignedOn = taskAssignedOn;
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
}
