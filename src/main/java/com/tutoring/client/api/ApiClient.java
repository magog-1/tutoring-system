package com.tutoring.client.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.*;

import java.io.IOException;
import java.util.Base64;

public class ApiClient {
    private static final String BASE_URL = "http://localhost:8080/api";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    private final OkHttpClient client;
    private final Gson gson;
    private String authToken;
    
    public ApiClient() {
        this.client = new OkHttpClient();
        this.gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .create();
    }
    
    public void setCredentials(String username, String password) {
        String credentials = username + ":" + password;
        this.authToken = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
    }
    
    public <T> T get(String endpoint, Class<T> responseClass) throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL + endpoint)
                .addHeader("Authorization", authToken)
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Ошибка: " + response.code());
            }
            String json = response.body().string();
            return gson.fromJson(json, responseClass);
        }
    }
    
    public <T> T post(String endpoint, Object requestBody, Class<T> responseClass) throws IOException {
        String json = gson.toJson(requestBody);
        RequestBody body = RequestBody.create(json, JSON);
        
        Request.Builder requestBuilder = new Request.Builder()
                .url(BASE_URL + endpoint)
                .post(body);
        
        if (authToken != null) {
            requestBuilder.addHeader("Authorization", authToken);
        }
        
        Request request = requestBuilder.build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                throw new IOException("Ошибка " + response.code() + ": " + errorBody);
            }
            String responseJson = response.body().string();
            if (responseClass == Void.class || responseJson.isEmpty()) {
                return null;
            }
            return gson.fromJson(responseJson, responseClass);
        }
    }
    
    public void put(String endpoint, Object requestBody) throws IOException {
        String json = requestBody != null ? gson.toJson(requestBody) : "";
        RequestBody body = RequestBody.create(json, JSON);
        
        Request request = new Request.Builder()
                .url(BASE_URL + endpoint)
                .put(body)
                .addHeader("Authorization", authToken)
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Ошибка: " + response.code());
            }
        }
    }
    
    public void delete(String endpoint) throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL + endpoint)
                .delete()
                .addHeader("Authorization", authToken)
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Ошибка: " + response.code());
            }
        }
    }
}
