package com.example.neptune.ttsapp;


import java.io.Serializable;

public class TaskDataModel implements Serializable {

    Long id;
    String deligationDateTime;
    String TaskDeligateOwnerUserID;
    String taskReceivedUserId;
    String projectNo;
    String projectName;
    String activityName;
    String taskName;
    String expectedDate;
    String expectedTotalTime;
    String actualTotalTime;
    String description;
    String modificationdescription;
    String seenOn;
    String acceptedOn;
    String completedOn;
    String status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDeligationDateTime() {
        return deligationDateTime;
    }

    public void setDeligationDateTime(String deligationDateTime) { this.deligationDateTime = deligationDateTime; }

    public String getTaskDeligateOwnerUserID() {
        return TaskDeligateOwnerUserID;
    }

    public void setTaskDeligateOwnerUserID(String taskDeligateOwnerUserID) { TaskDeligateOwnerUserID = taskDeligateOwnerUserID; }

    public String getTaskReceivedUserId() {
        return taskReceivedUserId;
    }

    public void setTaskReceivedUserId(String taskReceivedUserId) { this.taskReceivedUserId = taskReceivedUserId; }

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

    public String getSeenOn() {
        return seenOn;
    }

    public void setSeenOn(String seenOn) {
        this.seenOn = seenOn;
    }

    public String getAcceptedOn() {
        return acceptedOn;
    }

    public void setAcceptedOn(String acceptedOn) {
        this.acceptedOn = acceptedOn;
    }

    public String getCompletedOn() {
        return completedOn;
    }

    public void setCompletedOn(String completedOn) {
        this.completedOn = completedOn;
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
                ", deligationDateTime='" + deligationDateTime + '\'' +
                ", TaskDeligateOwnerUserID='" + TaskDeligateOwnerUserID + '\'' +
                ", taskReceivedUserId='" + taskReceivedUserId + '\'' +
                ", projectNo='" + projectNo + '\'' +
                ", projectName='" + projectName + '\'' +
                ", activityName='" + activityName + '\'' +
                ", taskName='" + taskName + '\'' +
                ", expectedDate='" + expectedDate + '\'' +
                ", expectedTotalTime='" + expectedTotalTime + '\'' +
                ", actualTotalTime='" + actualTotalTime + '\'' +
                ", description='" + description + '\'' +
                ", modificationdescription='" + modificationdescription + '\'' +
                ", seenOn='" + seenOn + '\'' +
                ", acceptedOn='" + acceptedOn + '\'' +
                ", completedOn='" + completedOn + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
