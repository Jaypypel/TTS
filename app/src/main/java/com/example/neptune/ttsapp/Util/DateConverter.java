package com.example.neptune.ttsapp.Util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateConverter {

    public static String getCurrentDateTime(){
        ZonedDateTime dateTimeInIst = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a");
        String currenDateAndTime = dateTimeInIst.format(formatter);
        return currenDateAndTime;
    }

    public static String currentDate(){
        ZonedDateTime dateTimeInIst = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String currentDate = dateTimeInIst.format(formatter);
        return currentDate;
    }
}
