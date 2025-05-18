package org.kivislime.weather;

import org.springframework.stereotype.Service;

@Service
public class LocationsService {
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final LocationMapper locationMapper;

    public LocationsService(LocationRepository locationRepository, UserRepository userRepository, LocationMapper locationMapper) {
        this.locationRepository = locationRepository;
        this.userRepository = userRepository;
        this.locationMapper = locationMapper;
    }

    public LocationDto addLocation(LocationRegistrationDto locationRegistrationDto, Long id) {
        Location locationRegistration = locationMapper.toEntity(locationRegistrationDto);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        locationRegistration.setUser(user);
        Location location = locationRepository.save(locationRegistration);
        return locationMapper.toDto(location);
    }
}
