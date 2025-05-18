package org.kivislime.weather;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class WeatherApiClient {
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${openweather.base-url}")
    private String baseUrl;

    @Value("${openweather.api-key}")
    private String apiKey;

    //TODO: обработка кодов ошибок со стороны OpenWeather. Условно неправильная длина
    public String fetchCurrentWeather(String latitude, String longitude) {
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("lat", latitude)
                .queryParam("lon", longitude)
                .queryParam("appid", apiKey)
                .queryParam("units", "metric")
                .build()
                .toUri();

        HttpRequest request = HttpRequest.newBuilder(uri)
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error sending request to OpenWeather",e);
        }
    }
}
