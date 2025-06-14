package org.kivislime.weather.service;

import lombok.extern.slf4j.Slf4j;
import org.kivislime.weather.client.GeocodingResponse;
import org.kivislime.weather.client.IWeatherApiClient;
import org.kivislime.weather.client.WeatherResponse;
import org.kivislime.weather.dto.LocationDto;
import org.kivislime.weather.dto.LocationSearchResultDto;
import org.kivislime.weather.dto.LocationWeatherDto;
import org.kivislime.weather.entity.Location;
import org.kivislime.weather.entity.User;
import org.kivislime.weather.exception.UserNotFoundException;
import org.kivislime.weather.mapper.WeatherMapper;
import org.kivislime.weather.repository.LocationRepository;
import org.kivislime.weather.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LocationService {
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final IWeatherApiClient weatherApiClient;
    private final WeatherMapper weatherMapper;
    private final WeatherCacheService weatherCacheService;
    private final ExecutorService executorService;
    private final LocationPersistenceService locationPersistenceService;

    public LocationService(LocationRepository locationRepository,
                           UserRepository userRepository, IWeatherApiClient weatherApiClient,
                           WeatherMapper weatherMapper,
                           WeatherCacheService weatherCacheService,
                           ExecutorService executorService,
                           LocationPersistenceService locationPersistenceService) {
        this.locationRepository = locationRepository;
        this.userRepository = userRepository;
        this.weatherApiClient = weatherApiClient;
        this.weatherMapper = weatherMapper;
        this.weatherCacheService = weatherCacheService;
        this.executorService = executorService;
        this.locationPersistenceService = locationPersistenceService;
    }

    public LocationDto addLocation(Long userId, String name) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("id: " + userId));

        WeatherResponse weather = weatherApiClient.fetchCurrentWeatherByName(name);
        log.info("User {} requested to add a location by name {}", user.getLogin(), name);
        Double lat = weather.getCoord().getLat();
        Double lon = weather.getCoord().getLon();
        Double temp = weather.getMain().getTemp();

        return locationPersistenceService.getOrCreate(user, name, lat, lon, temp);
    }

    @Transactional(readOnly = true)
    public List<LocationWeatherDto> getLocationsWithWeather(Long userId) {
        List<Location> locations = locationRepository.findByUserId(userId);
        if (locations.isEmpty()) {
            return Collections.emptyList();
        }

        List<CompletableFuture<LocationWeatherDto>> futures = locations.stream()
                .map(loc -> CompletableFuture.supplyAsync(() -> weatherCacheService.fetchWeather(loc), executorService))
                .toList();

        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    @Transactional
    public boolean removeLocation(Long userId, Double lat, Double lon) {
        long deletedCount = locationRepository.deleteByUserIdAndLatitudeAndLongitude(userId, lat, lon);
        log.info("User {} deleting location at lat={}, lon={}", userId, lat, lon);
        return deletedCount > 0;
    }

    public List<LocationSearchResultDto> searchLocationsListByName(String name) {
        List<GeocodingResponse> geocodingResponseList = weatherApiClient.fetchCurrentGeocodingByName(name);
        return geocodingResponseList.stream()
                .map(weatherMapper::toDto)
                .toList();
    }
}
