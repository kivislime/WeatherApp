package org.kivislime.weather;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

@Component
public class WeatherApiClient {
    private final RestTemplate restTemplate = new RestTemplate();

    //TODO: переделать в конструктор по идее
    @Value("${openweather.weather.url}")
    private String baseUrl;

    @Value("${openweather.geocoding.url}")
    private String geocodingUrl;

    @Value("${openweather.api-key}")
    private String apiKey;

    @Value("${openweather.geocoding.limit.cities}")
    private int maxCities;

    //TODO: обработка кодов ошибок со стороны OpenWeather. Условно неправильная длина
    // или бесплатный лимит на запросы закончился на этот час
    public WeatherResponse fetchCurrentWeatherByCoordinates(String latitude, String longitude) {
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("lat", latitude)
                .queryParam("lon", longitude)
                .queryParam("appid", apiKey)
                .queryParam("units", "metric")
                .build()
                .toUri();

        return restTemplate.getForObject(uri, WeatherResponse.class);
    }

    public WeatherResponse fetchCurrentWeatherByName(String cityName) {
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("q", cityName)
                .queryParam("appid", apiKey)
                .queryParam("units", "metric")
                .build()
                .toUri();

        return restTemplate.getForObject(uri, WeatherResponse.class);
    }

    public List<GeocodingResponse> fetchCurrentGeocodingByName(String cityName) {
        URI uri = UriComponentsBuilder.fromHttpUrl(geocodingUrl)
                .queryParam("q", cityName)
                .queryParam("limit", maxCities)
                .queryParam("appid", apiKey)
                .build()
                .toUri();

        GeocodingResponse[] geocodingResponseArray = restTemplate.getForObject(uri, GeocodingResponse[].class);

        return geocodingResponseArray == null ? List.of() : Arrays.asList(geocodingResponseArray);
    }
}
