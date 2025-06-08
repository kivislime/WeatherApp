package org.kivislime.weather.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kivislime.weather.exception.ExternalApiException;
import org.kivislime.weather.exception.LocationNotFoundException;
import org.kivislime.weather.exception.QuotaExceededException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

@Component
public class WeatherApiClient {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String geocodingUrl;
    private final String apiKey;
    private final int maxCities;

    public WeatherApiClient(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${openweather.weather.url}") String baseUrl,
            @Value("${openweather.geocoding.url}") String geocodingUrl,
            @Value("${openweather.api-key}") String apiKey,
            @Value("${openweather.geocoding.limit.cities}") int maxCities
    ) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
        this.geocodingUrl = geocodingUrl;
        this.apiKey = apiKey;
        this.maxCities = maxCities;
    }


    public WeatherResponse fetchCurrentWeatherByCoordinates(String latitude, String longitude) {
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("lat", latitude)
                .queryParam("lon", longitude)
                .queryParam("appid", apiKey)
                .queryParam("units", "metric")
                .build()
                .toUri();

        return fetchWeatherResponse(uri);
    }

    public WeatherResponse fetchCurrentWeatherByName(String cityName) {
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("q", cityName)
                .queryParam("appid", apiKey)
                .queryParam("units", "metric")
                .build()
                .toUri();

        return fetchWeatherResponse(uri);
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

    private WeatherResponse fetchWeatherResponse(URI uri) {
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    uri, HttpMethod.GET, HttpEntity.EMPTY, String.class);

            String responseBody = response.getBody();
            return objectMapper.readValue(responseBody, WeatherResponse.class);
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            HttpStatusCode statusCode = ex.getStatusCode();
            String errorBody = ex.getResponseBodyAsString();
            String apiMessage = parseErrorMessage(errorBody);

            if (statusCode == HttpStatus.NOT_FOUND) {
                throw new LocationNotFoundException(apiMessage);
            } else if (statusCode == HttpStatus.UNAUTHORIZED) {
                throw new ExternalApiException(apiMessage);
            } else if (statusCode == HttpStatus.TOO_MANY_REQUESTS) {
                throw new QuotaExceededException(apiMessage);
            } else {
                throw new ExternalApiException(
                        String.format("%s: %s", statusCode, apiMessage));
            }
        } catch (JsonProcessingException e) {
            throw new ExternalApiException("It is impossible to parse the response of the weather service");
        }
    }

    private String parseErrorMessage(String errorBody) {
        try {
            JsonNode node = objectMapper.readTree(errorBody);
            if (node.has("message")) {
                return node.get("message").asText();
            } else {
                return errorBody;
            }
        } catch (JsonProcessingException e) {
            return errorBody;
        }
    }
}
