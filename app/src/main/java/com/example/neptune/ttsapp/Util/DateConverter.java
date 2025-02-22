package com.example.neptune.ttsapp.Util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DateConverter {

    public static String getCurrentDateTime(){
        ZonedDateTime dateTimeInIst = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a",Locale.ENGLISH);
        return dateTimeInIst.format(formatter);
    }

    public static String currentDate(){
        ZonedDateTime dateTimeInIst = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH);
        return dateTimeInIst.format(formatter);
    }

    public static String currentTime(){
        ZonedDateTime dateTimeInIst = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a",Locale.ENGLISH);
        return dateTimeInIst.format(formatter);
    }
}
