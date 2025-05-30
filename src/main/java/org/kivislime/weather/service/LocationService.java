package org.kivislime.weather.service;

import jakarta.transaction.Transactional;
    import lombok.extern.slf4j.Slf4j;
import org.kivislime.weather.dto.LocationDto;
import org.kivislime.weather.dto.LocationRegistrationDto;
import org.kivislime.weather.dto.LocationWeatherDto;
import org.kivislime.weather.entity.Location;
import org.kivislime.weather.entity.User;
import org.kivislime.weather.exception.LocationLimitExceededException;
import org.kivislime.weather.exception.UserNotFoundException;
import org.kivislime.weather.mapper.LocationMapper;
import org.kivislime.weather.repository.LocationRepository;
import org.kivislime.weather.repository.UserRepository;
import org.kivislime.weather.client.WeatherApiClient;
import org.kivislime.weather.client.WeatherResponse;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LocationService {
    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;
    private final UserRepository userRepository;
    private final WeatherApiClient weatherApiClient;
    private final String iconBaseUrl;
    private final int maxLocationsPerUser;

    public LocationService(
            LocationRepository locationRepository,
            LocationMapper locationMapper,
            UserRepository userRepository,
            WeatherApiClient weatherApiClient,
            @Value("${openweather.icon-base-url}") String iconBaseUrl,
            //TODO: создать отдельный класс настроек для всякой app.location, scheduler.pool-size=1? Чтобы без таких приколов
            @Value("${app.location.max-per-user}") int maxLocationsPerUser
    ) {
        this.locationRepository = locationRepository;
        this.locationMapper = locationMapper;
        this.userRepository = userRepository;
        this.weatherApiClient = weatherApiClient;
        this.iconBaseUrl = iconBaseUrl;
        this.maxLocationsPerUser = maxLocationsPerUser;
    }

    @Transactional
    public LocationDto addLocation(Long userId, Double temperature, LocationRegistrationDto locationRegistrationDto) {
        Location locationRegistration = locationMapper.toEntity(locationRegistrationDto);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(String.format("id: %s", userId)));
        locationRegistration.setUser(user);

        long countLocations = locationRepository.countByUser(user);

        if (countLocations >= maxLocationsPerUser) {
            throw new LocationLimitExceededException(String.format("user_id: %s", userId));
        }

        Location location = locationRepository.findByUserIdAndLatitudeAndLongitude(userId,
                        locationRegistrationDto.getLatitude(),
                        locationRegistration.getLongitude())
                .orElseGet(() -> locationRepository.save(locationRegistration));

        return locationMapper.toDto(location, temperature);
    }

    public List<LocationWeatherDto> getLocationsWithWeather(Long userId) {
        List<Location> locations = locationRepository.findByUserId(userId);
        if (locations.isEmpty()) {
            return Collections.emptyList();
        }

        List<CompletableFuture<LocationWeatherDto>> futures = locations.stream()
                .map(loc -> CompletableFuture.supplyAsync(() -> {
                    WeatherResponse weather = weatherApiClient.fetchCurrentWeatherByCoordinates(
                            loc.getLatitude().toString(),
                            loc.getLongitude().toString()
                    );
                    return new LocationWeatherDto(
                            loc.getId(),
                            loc.getName(),
                            loc.getUser().getId(),
                            loc.getLatitude(),
                            loc.getLongitude(),

                            weather.getMain().getTemp(),
                            weather.getMain().getFeelsLike(),
                            weather.getWeather().get(0).getDescription(),
                            weather.getMain().getHumidity(),
                            String.format("%s%s@2x.png", iconBaseUrl, weather.getWeather().get(0).getIcon())
                    );
                }))
                .toList();

        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    @Transactional
    public boolean removeLocation(Long id, String lat, String lon) {
        try {
            Double latDouble = Double.parseDouble(lat);
            Double lonDouble = Double.parseDouble(lon);

            long deletedCount = locationRepository.deleteByUserIdAndLatitudeAndLongitude(id, latDouble, lonDouble);

            return deletedCount > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
