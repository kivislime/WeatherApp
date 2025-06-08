package org.kivislime.weather.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kivislime.weather.dto.LocationDto;
import org.kivislime.weather.entity.Location;
import org.kivislime.weather.entity.User;
import org.kivislime.weather.exception.LocationLimitExceededException;
import org.kivislime.weather.mapper.LocationMapper;
import org.kivislime.weather.repository.LocationRepository;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationPersistenceServiceTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private LocationMapper locationMapper;

    private LocationPersistenceService persistenceService;

    private User dummyUser;
    private Location existingLocationEntity;
    private Location unsavedLocationEntity;
    private LocationDto existingDto;
    private final int maxLocationsPerUser = 3;

    @BeforeEach
    void setUp() {
        persistenceService = new LocationPersistenceService(
                locationMapper,
                locationRepository,
                maxLocationsPerUser
        );

        dummyUser = new User();
        dummyUser.setId(42L);
        dummyUser.setLogin("alice");
        dummyUser.setPassword("hashedPwd");

        existingLocationEntity = new Location();
        existingLocationEntity.setId(100L);
        existingLocationEntity.setName("CityA");
        existingLocationEntity.setLatitude(10.0);
        existingLocationEntity.setLongitude(20.0);
        existingLocationEntity.setUser(dummyUser);

        unsavedLocationEntity = new Location();
        unsavedLocationEntity.setName("CityA");
        unsavedLocationEntity.setLatitude(10.0);
        unsavedLocationEntity.setLongitude(20.0);
        unsavedLocationEntity.setUser(dummyUser);

        existingDto = new LocationDto(
                100L,
                "CityA",
                42L,
                10.0,
                20.0,
                25.0
        );
    }

    @Test
    void getOrCreate_WhenCountAtLimit_ShouldThrowLocationLimitExceededException() {
        when(locationRepository.countByUser(dummyUser)).thenReturn((long) maxLocationsPerUser);

        assertThatThrownBy(() ->
                persistenceService.getOrCreate(dummyUser, "CityB", 15.0, 25.0, 30.0)
        ).isInstanceOf(LocationLimitExceededException.class)
                .hasMessageContaining("user_id: " + dummyUser.getId());

        verify(locationRepository, never()).findByUserIdAndLatitudeAndLongitude(anyLong(), anyDouble(), anyDouble());
        verify(locationRepository, never()).save(any());
    }

    @Test
    void getOrCreate_WhenLocationExists_ShouldReturnDtoWithoutSaving() {
        when(locationRepository.countByUser(dummyUser)).thenReturn(1L);
        when(locationRepository.findByUserIdAndLatitudeAndLongitude(
                eq(dummyUser.getId()),
                eq(existingLocationEntity.getLatitude()),
                eq(existingLocationEntity.getLongitude())
        )).thenReturn(Optional.of(existingLocationEntity));

        when(locationMapper.toDto(existingLocationEntity, 30.0)).thenReturn(existingDto);

        LocationDto result = persistenceService.getOrCreate(
                dummyUser,
                existingLocationEntity.getName(),
                existingLocationEntity.getLatitude(),
                existingLocationEntity.getLongitude(),
                30.0
        );

        assertThat(result).isEqualTo(existingDto);

        verify(locationRepository).countByUser(dummyUser);
        verify(locationRepository).findByUserIdAndLatitudeAndLongitude(
                dummyUser.getId(),
                existingLocationEntity.getLatitude(),
                existingLocationEntity.getLongitude()
        );
        verify(locationRepository, never()).save(any());
        verify(locationMapper).toDto(existingLocationEntity, 30.0);
    }

    @Test
    void getOrCreate_WhenNewLocation_ShouldSaveAndReturnDto() {
        when(locationRepository.countByUser(dummyUser)).thenReturn(1L);
        when(locationRepository.findByUserIdAndLatitudeAndLongitude(
                eq(dummyUser.getId()),
                eq(unsavedLocationEntity.getLatitude()),
                eq(unsavedLocationEntity.getLongitude())
        )).thenReturn(Optional.empty());

        when(locationRepository.save(any(Location.class))).thenReturn(existingLocationEntity);
        when(locationMapper.toDto(existingLocationEntity, 40.0)).thenReturn(existingDto);

        LocationDto result = persistenceService.getOrCreate(
                dummyUser,
                "CityA",
                10.0,
                20.0,
                40.0
        );

        assertThat(result).isEqualTo(existingDto);

        verify(locationRepository).save(argThat(loc ->
                loc.getName().equals("CityA") &&
                        loc.getLatitude().equals(10.0) &&
                        loc.getLongitude().equals(20.0) &&
                        loc.getUser().equals(dummyUser)
        ));

        verify(locationMapper).toDto(existingLocationEntity, 40.0);
    }

    @Test
    void getOrCreate_WhenSaveThrowsDataIntegrityViolation_ShouldReturnExistingDto() {
        when(locationRepository.countByUser(dummyUser)).thenReturn(1L);

        when(locationRepository.findByUserIdAndLatitudeAndLongitude(
                eq(dummyUser.getId()),
                eq(10.0),
                eq(20.0)
        )).thenReturn(Optional.empty())
                .thenReturn(Optional.of(existingLocationEntity));

        when(locationRepository.save(any(Location.class)))
                .thenThrow(new DataIntegrityViolationException("Constraint violation"));

        when(locationMapper.toDto(existingLocationEntity, 50.0)).thenReturn(existingDto);

        LocationDto result = persistenceService.getOrCreate(
                dummyUser,
                "CityA",
                10.0,
                20.0,
                50.0
        );

        assertThat(result).isEqualTo(existingDto);

        verify(locationRepository).save(any(Location.class));
        verify(locationRepository, times(2)).findByUserIdAndLatitudeAndLongitude(
                dummyUser.getId(),
                10.0,
                20.0
        );
        verify(locationMapper).toDto(existingLocationEntity, 50.0);
    }
}
