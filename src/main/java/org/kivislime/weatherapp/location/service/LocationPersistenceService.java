package org.kivislime.weatherapp.location.service;

import jakarta.transaction.Transactional;
import org.kivislime.weatherapp.location.entity.Location;
import org.kivislime.weatherapp.location.repository.LocationRepository;
import org.kivislime.weatherapp.location.dto.LocationDto;
import org.kivislime.weatherapp.location.exception.LocationLimitExceededException;
import org.kivislime.weatherapp.location.mapper.LocationMapper;
import org.kivislime.weatherapp.user.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class LocationPersistenceService {
    private final LocationMapper locationMapper;
    private final LocationRepository locationRepository;
    private final int maxLocationsPerUser;

    public LocationPersistenceService(LocationMapper locationMapper,
                                      LocationRepository locationRepository,
                                      @Value("${app.location.max-per-user}") int maxLocationsPerUser
    ) {
        this.locationMapper = locationMapper;
        this.locationRepository = locationRepository;
        this.maxLocationsPerUser = maxLocationsPerUser;
    }

    @Transactional
    public LocationDto getOrCreate(User user, String name, Double lat, Double lon, Double temp) {
        LocationDto location = locationRepository.findByUserIdAndLatitudeAndLongitude(user.getId(), lat, lon)
                .map(existing -> locationMapper.toDto(existing, temp))
                .orElseGet(() -> tryCreateLocation(user, name, lat, lon, temp));

        if (locationRepository.countByUser(user) > maxLocationsPerUser) {
            throw new LocationLimitExceededException("user_id: " + user.getId());
        }

        return location;
    }

    private LocationDto tryCreateLocation(User user, String name, Double lat, Double lon, Double temp) {
        try {
            Location newLocation = new Location();
            newLocation.setName(name);
            newLocation.setLatitude(lat);
            newLocation.setLongitude(lon);
            newLocation.setUser(user);
            Location saved = locationRepository.save(newLocation);
            return locationMapper.toDto(saved, temp);
        } catch (DataIntegrityViolationException ex) {
            return locationRepository.findByUserIdAndLatitudeAndLongitude(user.getId(), lat, lon)
                    .map(location -> locationMapper.toDto(location, temp))
                    .orElseThrow(() -> new IllegalStateException("Unique constraint failed but record not found"));
        }
    }
}