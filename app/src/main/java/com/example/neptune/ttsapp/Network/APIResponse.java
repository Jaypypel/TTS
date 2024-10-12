package com.example.neptune.ttsapp.Network;

import java.util.Collections;
import java.util.Map;

import retrofit2.Response;

public abstract class APIResponse<T>{

    public static  <T> APIErrorResponse<T> create(Throwable err){
        return new APIErrorResponse<>(err.getMessage() != null ? err.getMessage():"unknown error");
    }

    public static <T> APIResponse<T> create(Response<T> response) {
        if (response.isSuccessful()) {
            T body = response.body();
            if (body == null || response.code() == 204) return new APIEmptyResponse<>();
            Map<String, String> linkheader = Collections.emptyMap();
            linkheader.put("link", response.headers().get("link"));
            return new APISuccessResponse<>(body, linkheader);
        }
        String msg = null;
        if (response.errorBody() != null){
            try {
                msg = response.errorBody().string();
            } catch (Exception e) {
                System.out.println(e + "Error reading error body");
            }
        }
        String errorMsg = (msg != null && !msg.isEmpty() ? msg: response.message());
        return new APIErrorResponse<>(errorMsg != null ? errorMsg: "unknown error");
    }
}
