package org.kivislime.weatherapp.weather.service;

import lombok.extern.slf4j.Slf4j;
import org.kivislime.weatherapp.weather.client.IWeatherApiClient;
import org.kivislime.weatherapp.weather.dto.WeatherResponse;
import org.kivislime.weatherapp.location.dto.LocationWeatherDto;
import org.kivislime.weatherapp.location.entity.Location;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WeatherCacheService {
    private final IWeatherApiClient weatherApiClient;
    private final String iconBaseUrl;

    public WeatherCacheService(IWeatherApiClient weatherApiClient,
                               @Value("${openweather.icon-base-url}") String iconBaseUrl) {
        this.weatherApiClient = weatherApiClient;
        this.iconBaseUrl = iconBaseUrl;
    }

    @Cacheable(cacheNames = "weatherByLocation", key = "#p0.latitude + ',' + #p0.longitude")
    public LocationWeatherDto fetchWeather(Location loc) {
        WeatherResponse weather = weatherApiClient.fetchCurrentWeatherByCoordinates(
                loc.getLatitude().toString(),
                loc.getLongitude().toString()
        );
        log.info(">>> Actual fetchWeather() called for lat={}, lon={}", loc.getLatitude(), loc.getLongitude());

        String icon = weather.getWeather().isEmpty()
                ? "unknown"
                : weather.getWeather().get(0).getIcon();

        return new LocationWeatherDto(
                loc.getId(),
                loc.getName(),
                loc.getUser().getId(),
                loc.getLatitude(),
                loc.getLongitude(),

                weather.getMain().getTemp(),
                weather.getMain().getFeelsLike(),
                weather.getWeather().isEmpty() ? "" : weather.getWeather().get(0).getDescription(),
                weather.getMain().getHumidity(),
                String.format("%s%s@2x.png", iconBaseUrl, icon)
        );
    }
}
