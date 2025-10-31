package com.example.neptune.ttsapp.Network;

import androidx.annotation.NonNull;

import com.example.neptune.ttsapp.SessionManager;

import java.io.IOException;

import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class BasicAuthInterceptor implements Interceptor{
//    private volatile String credentials;
    private final SessionManager sessionManager;
    public BasicAuthInterceptor(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    /**
     * @param chain
     * @return
     * @throws IOException
     */
    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
//        Request.Builder builder = chain.request().newBuilder();
//        if( credentials!=null ){
//            builder.header("Authorization", credentials);
//        }
//        return chain.proceed(builder.build());

        Request request = chain.request();
        String jwt = sessionManager.getJWT();
        if (jwt != null) {
            request = request.newBuilder()
                    .header("Authorization", "Bearer " + jwt)
                    .build();
        }
        return chain.proceed(request);
    }

//    public synchronized void setCredentials(String username, String password) {
//        if(username != null && password != null){
//            this.credentials = Credentials.basic(username, password);
//        }else this.credentials = null;
//    }
//
//    public synchronized void clearCredentials() {
//        this.credentials = null;
//    }
//
//    public String getCredentials() {
//        return credentials;
//    }
}
