package com.tutoring.client.api;

import com.google.gson.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GsonProvider {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static Gson instance;
    
    public static Gson getGson() {
        if (instance == null) {
            instance = new GsonBuilder()
                    .registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
                        @Override
                        public LocalDateTime deserialize(JsonElement json, java.lang.reflect.Type typeOfT,
                                                         JsonDeserializationContext context) throws JsonParseException {
                            if (json.isJsonNull()) {
                                return null;
                            }
                            String dateTimeString = json.getAsString();
                            try {
                                return LocalDateTime.parse(dateTimeString, FORMATTER);
                            } catch (Exception e) {
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
        return instance;
    }
}
