package org.kivislime.weather.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.kivislime.weather.dto.LocationDto;
import org.kivislime.weather.dto.LocationRegistrationDto;
import org.kivislime.weather.dto.LocationWeatherDto;
import org.kivislime.weather.entity.Location;
import org.kivislime.weather.entity.User;
import org.kivislime.weather.exception.UserNotFoundException;
import org.kivislime.weather.mapper.LocationMapper;
import org.kivislime.weather.repository.LocationRepository;
import org.kivislime.weather.repository.UserRepository;
import org.kivislime.weather.client.WeatherApiClient;
import org.kivislime.weather.client.WeatherResponse;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocationsService {
    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;
    private final UserRepository userRepository;
    private final WeatherApiClient weatherApiClient;

    public LocationDto addLocation(Long userId, Double temperature, LocationRegistrationDto locationRegistrationDto) {
        Location locationRegistration = locationMapper.toEntity(locationRegistrationDto);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(String.format("User not found id: %s", userId)));

        locationRegistration.setUser(user);
        Location location = locationRepository.save(locationRegistration);
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
                    //TODO: тоже строки картинки захардкожена
                    //How to get icon URL
                    //For code 500 - light rain icon = "10d". See below a full list of codes
                    //URL is https://openweathermap.org/img/wn/10d@2x.png
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
                            "https://openweathermap.org/img/wn/"
                                    + weather.getWeather().get(0).getIcon() + "@2x.png"
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
