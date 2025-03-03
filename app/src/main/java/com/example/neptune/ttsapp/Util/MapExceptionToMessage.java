package com.example.neptune.ttsapp.Util;

import java.net.SocketTimeoutException;
import java.util.concurrent.CompletionException;

public class MapExceptionToMessage {

    public static String getMessageFromThrownException(Throwable e){
        if (e instanceof SocketTimeoutException){
            return "Time out ! Retry it";
        }

        if(e instanceof CompletionException){
            return "Server is down";
        }
        return e.getMessage();
    }
}
