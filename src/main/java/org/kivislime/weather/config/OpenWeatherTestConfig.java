package org.kivislime.weather.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kivislime.weather.client.IWeatherApiClient;
import org.kivislime.weather.client.WeatherApiClientImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
@Profile("test")
public class OpenWeatherTestConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate(new SimpleClientHttpRequestFactory());
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
