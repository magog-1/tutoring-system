package com.tutoring.client.api;

import com.google.gson.*;
import okhttp3.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

public class ApiClient {
    private static final String BASE_URL = "http://localhost:8080/api";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    
    private final OkHttpClient client;
    private final Gson gson;
    private String authToken;
    
    public ApiClient() {
        this.client = new OkHttpClient();
        
        // Создаём Gson с кастомным адаптером для LocalDateTime
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
                    @Override
                    public LocalDateTime deserialize(JsonElement json, java.lang.reflect.Type typeOfT,
                                                     JsonDeserializationContext context) throws JsonParseException {
                        if (json.isJsonNull()) {
                            return null;
                        }
                        String dateTimeString = json.getAsString();
                        // Поддерживаем разные форматы
                        try {
                            return LocalDateTime.parse(dateTimeString, FORMATTER);
                        } catch (Exception e) {
                            // Пробуем ISO формат
                            return LocalDateTime.parse(dateTimeString);
                        }
                    }
                })
                .registerTypeAdapter(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
                    @Override
                    public JsonElement serialize(LocalDateTime src, java.lang.reflect.Type typeOfSrc,
                                                 JsonSerializationContext context) {
                        return new JsonPrimitive(src.format(FORMATTER));
                    }
                })
                .setLenient()
                .create();
    }
    
    public void setCredentials(String username, String password) {
        String credentials = username + ":" + password;
        this.authToken = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
    }
    
    public void setAuthToken(String token) {
        this.authToken = "Bearer " + token;
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
            
            // Если запрашивается String.class - возвращаем сырую строку без парсинга
            if (responseClass == String.class) {
                return (T) json;
            }
            
            // Для остальных типов - парсим
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
            
            // Если запрашивается String.class - возвращаем сырую строку
            if (responseClass == String.class) {
                return (T) responseJson;
            }
            
            return gson.fromJson(responseJson, responseClass);
        }
    }
    
    // Перегруженный метод для прямой передачи JSON строки
    public String post(String endpoint, String jsonBody) throws IOException {
        RequestBody body = RequestBody.create(jsonBody, JSON);
        
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
            return response.body().string();
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
    
    // Перегруженный метод для прямой передачи JSON строки
    public void put(String endpoint, String jsonBody) throws IOException {
        RequestBody body = RequestBody.create(jsonBody, JSON);
        
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
