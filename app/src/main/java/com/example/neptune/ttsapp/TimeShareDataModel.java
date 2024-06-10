package com.example.neptune.ttsapp;

import java.io.Serializable;

public class TimeShareDataModel implements Serializable {

    String timeShareDate;
    String startTime;
    String endTime;
    String timeDifference;
    String timeShareDescription;

    public String getTimeShareDate() {
        return timeShareDate;
    }

    public void setTimeShareDate(String timeShareDate) {
        this.timeShareDate = timeShareDate;
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
        return timeDifference;
    }

    public void setTimeDifference(String timeDifference) {
        this.timeDifference = timeDifference;
    }

    public String getTimeShareDescription() {
        return timeShareDescription;
    }

    public void setTimeShareDescription(String timeShareDescription) {
        this.timeShareDescription = timeShareDescription;
    }
}
