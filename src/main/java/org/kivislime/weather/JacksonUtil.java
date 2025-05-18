package org.kivislime.weather;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonUtil {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static WeatherResponse toWeatherResponse(String json) {
        try {
            return mapper.readValue(json, WeatherResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error deserialization WeatherResponse class", e);
        }
    }

    public static String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serialization object ", e);
        }
    }
}
