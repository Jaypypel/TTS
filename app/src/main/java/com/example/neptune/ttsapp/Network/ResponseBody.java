package com.example.neptune.ttsapp.Network;

public class ResponseBody<T> {

    private String message;
    private T body;


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "{\"message\": \" " + message + "\",\"body\":\" " + body + "\"}";
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }
}
