package com.example.neptune.ttsapp.Network;

import com.fasterxml.jackson.databind.JsonNode;

public class ResponseBody {

    private String message;
    private JsonNode body;


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

    public JsonNode getBody() {
        return body;
    }

    public void setBody(JsonNode body) {
        this.body = body;
    }
}
