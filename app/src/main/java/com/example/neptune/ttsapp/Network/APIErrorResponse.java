package com.example.neptune.ttsapp.Network;


public class APIErrorResponse<T> extends APIResponse<T>{

    private final String errorMessage;

    public APIErrorResponse(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}
