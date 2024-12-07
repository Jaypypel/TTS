package com.example.neptune.ttsapp;

import java.io.Serializable;

public class TimeShareDataModel implements Serializable {

    String dateOfTimeShare;
    String startTime;

    @Override
    public String toString() {
        return "TimeShareDataModel{" +
                "dateOfTimeShare='" + dateOfTimeShare + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", timeDifference='" + timeDifference + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    String endTime;
    String timeDifference;
    String description;

    public String getdateOfTimeShare() {
        return dateOfTimeShare;
    }

    public void setdateOfTimeShare(String dateOfTimeShare) {
        this.dateOfTimeShare = dateOfTimeShare;
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

    public String getdescription() {
        return description;
    }

    public void setdescription(String description) {
        this.description = description;
    }
}
