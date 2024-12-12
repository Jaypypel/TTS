package com.example.neptune.ttsapp.Network;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
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
            Map<String, String> linkheader = new HashMap<>();
            linkheader.put("link", response.headers().get("link"));
            return new APISuccessResponse<>(body, linkheader);
        }

        String msg = "";
        if (response.isSuccessful() && response.body() instanceof ResponseBody) {
             msg =((ResponseBody)response.body()).getMessage().getAsString();
             return new APIErrorResponse<>(msg);
        } else  if (response.errorBody() != null && !(response.body() instanceof ResponseBody)){
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
