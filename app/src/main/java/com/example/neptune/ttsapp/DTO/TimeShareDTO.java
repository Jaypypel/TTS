package com.example.neptune.ttsapp.DTO;

public class TimeShareDTO {

    private Long taskHandlerId;

    private String date;

    private String startTime;

    private String endTime;

    private String TimeDifference;

    private String description;

    private String createdOn;

    public Long getTaskHandlerId() {
        return taskHandlerId;
    }

    public void setTaskHandlerId(Long taskHandlerId) {
        this.taskHandlerId = taskHandlerId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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

    public String getTimeDifference() {
        return TimeDifference;
    }

    public void setTimeDifference(String timeDifference) {
        TimeDifference = timeDifference;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }
}
