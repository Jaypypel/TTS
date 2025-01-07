package com.example.neptune.ttsapp.Network;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Response;

public  class APIResponse<T>{

    public APIResponse() {

    }

    public static  <T> APIErrorResponse<T> create(Throwable err){
        return new APIErrorResponse<>(err.getMessage() != null ? err.getMessage():"unknown error");
    }

    public static <T> APIResponse<T> create(Response<T> response) throws IOException {
        if (response.isSuccessful()) {
            T body = response.body();
            if (body == null || response.code() == 204) return new APIEmptyResponse<>();
//            Map<String, String> linkheader = new HashMap<>();
//            linkheader.put("link", response.headers().get("link"));
            return new APISuccessResponse<>(body);

        }

        String msg = "";

       if (response.errorBody() != null){
            try {
                msg = response.errorBody().string();
            } catch (Exception e) {
                System.out.println(e + "Error reading error body");
            }
        }
        String errorMsg = (msg != null && !msg.isEmpty() ? msg: response.message());
//        String errorMsg = response.errorBody() != null ? response.errorBody().string() : response.message();
        return new APIErrorResponse<>(errorMsg != null ? errorMsg: "unknown error");
    }
}
