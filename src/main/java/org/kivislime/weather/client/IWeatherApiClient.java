package org.kivislime.weather.client;

import java.util.List;

public interface IWeatherApiClient {
    WeatherResponse fetchCurrentWeatherByCoordinates(String latitude, String longitude);

    WeatherResponse fetchCurrentWeatherByName(String cityName);

    List<GeocodingResponse> fetchCurrentGeocodingByName(String cityName);
}
