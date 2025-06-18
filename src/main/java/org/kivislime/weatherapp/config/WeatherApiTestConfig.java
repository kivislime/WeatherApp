package org.kivislime.weatherapp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kivislime.weatherapp.weather.client.IWeatherApiClient;
import org.kivislime.weatherapp.weather.client.WeatherApiClientImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

@Configuration
@Profile("test")
public class WeatherApiTestConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public IWeatherApiClient weatherApiClient(
            RestTemplate restTemplate,
            ObjectMapper objectMapper
    ) {
        return new WeatherApiClientImpl(
                restTemplate,
                objectMapper,
                "http://api.openweathermap.org/data/2.5/weather",
                "http://api.openweathermap.org/geo/1.0/direct",
                "DUMMY_KEY",
                5
        );
    }
}
