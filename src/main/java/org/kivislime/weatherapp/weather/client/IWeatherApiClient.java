package org.kivislime.weatherapp.weather.client;

import org.kivislime.weatherapp.weather.dto.GeocodingResponse;
import org.kivislime.weatherapp.weather.dto.WeatherResponse;

import java.util.List;

public interface IWeatherApiClient {
    WeatherResponse fetchCurrentWeatherByCoordinates(String latitude, String longitude);

    WeatherResponse fetchCurrentWeatherByName(String cityName);

    List<GeocodingResponse> fetchCurrentGeocodingByName(String cityName);
}
