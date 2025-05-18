package org.kivislime.weather;

import org.springframework.stereotype.Service;

@Service
public class LocationsService {
    private final LocationRepository locationRepository;

    public LocationsService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public LocationDto addLocation(LocationRegistrationDto locationRegistrationDto) {
        return null;
    }
}
