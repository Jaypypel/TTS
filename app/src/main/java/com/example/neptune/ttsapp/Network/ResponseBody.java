package com.example.neptune.ttsapp.Network;



import com.google.gson.JsonElement;


public class ResponseBody {

    private JsonElement message;
    private JsonElement body;

    public ResponseBody() {
    }

    public JsonElement getMessage() {
        return message;
    }

    public void setMessage(JsonElement message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "{\"message\": \" " + message + "\",\"body\":\" " + body + "\"}";
    }

    public JsonElement getBody() {
        return body;
    }

    public void setBody(JsonElement body) {
        this.body = body;
    }
}
