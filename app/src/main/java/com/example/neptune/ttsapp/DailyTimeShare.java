package com.example.neptune.ttsapp;

public class DailyTimeShare {

    private String dateOfTimeShare;

    private String projectCode;

    private String projectName;

    private String activityName;

    private String taskName;

    private String startTime;

    private String endTime;

    private String timeDifference;

    private String description;

    private String createdOn;

    private String username;

    public DailyTimeShare(String dateOfTimeShare, String projectCode, String projectName, String activityName, String taskName, String startTime, String endTime, String timeDifference, String description, String createdOn, String username) {
        this.dateOfTimeShare = dateOfTimeShare;
        this.projectCode = projectCode;
        this.projectName = projectName;
        this.activityName = activityName;
        this.taskName = taskName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.timeDifference = timeDifference;
        this.description = description;
        this.createdOn = createdOn;
        this.username = username;
    }

}
