package org.kivislime.weather;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
                .orElseThrow(() -> new RuntimeException("User not found"));

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
