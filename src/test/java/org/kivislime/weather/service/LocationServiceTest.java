package org.kivislime.weather.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kivislime.weather.client.WeatherResponse;
import org.kivislime.weather.client.WeatherApiClient;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private LocationMapper locationMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WeatherApiClient weatherApiClient;

    private final String iconBaseUrl = "http://openweathermap.org/img/";
    private final int maxLocationsPerUser = 5;

    private LocationService locationService;

    private User dummyUser;
    private LocationRegistrationDto registrationDto;
    private Location existingLocationEntity;
    private Location unsavedLocationEntity;

    @BeforeEach
    void setUp() {
        locationService = new LocationService(
                locationRepository,
                locationMapper,
                userRepository,
                weatherApiClient,
                iconBaseUrl,
                maxLocationsPerUser
        );

        dummyUser = new User();
        dummyUser.setId(1L);
        dummyUser.setLogin("john");
        dummyUser.setPassword("pwd");

        registrationDto = new LocationRegistrationDto(
                "TestCity",
                55.75,
                37.61
        );

        existingLocationEntity = new Location();
        existingLocationEntity.setId(100L);
        existingLocationEntity.setName("TestCity");
        existingLocationEntity.setLatitude(55.75);
        existingLocationEntity.setLongitude(37.61);
        existingLocationEntity.setUser(dummyUser);

        unsavedLocationEntity = new Location();
        unsavedLocationEntity.setName("TestCity");
        unsavedLocationEntity.setLatitude(55.75);
        unsavedLocationEntity.setLongitude(37.61);
    }

    @Test
    void addLocation_UserNotFound_ShouldThrowUserNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> locationService.addLocation(1L, 20.0, registrationDto)
        );

        verify(locationRepository, never()).countByUser(any());
        verify(locationRepository, never()).save(any());
    }

    @Test
    void addLocation_ExceededLimit_ShouldThrowLocationLimitExceededException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(dummyUser));
        when(locationRepository.countByUser(dummyUser)).thenReturn((long) maxLocationsPerUser);

        lenient().when(locationMapper.toEntity(any(LocationRegistrationDto.class)))
                .thenReturn(unsavedLocationEntity);

        assertThrows(
                LocationLimitExceededException.class,
                () -> locationService.addLocation(1L, 15.0, registrationDto)
        );

        verify(locationRepository, never()).save(any());
    }

    @Test
    void addLocation_LocationAlreadyExists_ShouldNotCallSave_ReturnDtoViaMapper() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(dummyUser));
        when(locationRepository.countByUser(dummyUser)).thenReturn(2L);

        lenient().when(locationMapper.toEntity(any(LocationRegistrationDto.class)))
                .thenReturn(unsavedLocationEntity);

        when(locationRepository.findByUserIdAndLatitudeAndLongitude(
                eq(1L),
                eq(registrationDto.getLatitude()),
                eq(registrationDto.getLongitude())
        )).thenReturn(Optional.of(existingLocationEntity));

        double dummyTemp = 15.0;
        LocationDto mappedDto = new LocationDto(
                100L,
                "TestCity",
                1L,
                55.75,
                37.61,
                dummyTemp
        );
        when(locationMapper.toDto(existingLocationEntity, dummyTemp)).thenReturn(mappedDto);

        LocationDto result = locationService.addLocation(1L, dummyTemp, registrationDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getName()).isEqualTo("TestCity");

        verify(locationRepository, never()).save(any());
        verify(locationMapper).toDto(existingLocationEntity, dummyTemp);
    }

    @Captor
    ArgumentCaptor<Location> locationCaptor;

    @Test
    void addLocation_NewLocation_ShouldCallSave_AndReturnDtoViaMapper() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(dummyUser));
        when(locationRepository.countByUser(dummyUser)).thenReturn(1L);

        when(locationRepository.findByUserIdAndLatitudeAndLongitude(
                eq(1L),
                eq(registrationDto.getLatitude()),
                eq(registrationDto.getLongitude())
        )).thenReturn(Optional.empty());

        lenient().when(locationMapper.toEntity(any(LocationRegistrationDto.class)))
                .thenReturn(unsavedLocationEntity);

        when(locationRepository.save(any(Location.class))).thenReturn(existingLocationEntity);

        double dummyTemp = 15.0;
        LocationDto mappedDto = new LocationDto(
                100L,
                "TestCity",
                1L,
                55.75,
                37.61,
                dummyTemp
        );
        when(locationMapper.toDto(existingLocationEntity, dummyTemp)).thenReturn(mappedDto);

        LocationDto result = locationService.addLocation(1L, dummyTemp, registrationDto);

        verify(locationRepository).save(locationCaptor.capture());
        Location savedLocation = locationCaptor.getValue();
        assertThat(savedLocation.getName()).isEqualTo("TestCity");
        assertThat(savedLocation.getLatitude()).isEqualTo(55.75);
        assertThat(savedLocation.getLongitude()).isEqualTo(37.61);
        assertThat(savedLocation.getUser()).isEqualTo(dummyUser);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getName()).isEqualTo("TestCity");
    }

    @Test
    void getLocationsWithWeather_NoLocations_ReturnsEmptyList() {
        when(locationRepository.findByUserId(1L)).thenReturn(List.of());

        List<LocationWeatherDto> result = locationService.getLocationsWithWeather(1L);
        assertThat(result).isEmpty();
        verify(weatherApiClient, never()).fetchCurrentWeatherByCoordinates(anyString(), anyString());
    }

    @Test
    void getLocationsWithWeather_OneLocation_ShouldReturnListWithOneDto() {
        when(locationRepository.findByUserId(1L)).thenReturn(List.of(existingLocationEntity));

        WeatherResponse.MainInfo mainInfo = new WeatherResponse.MainInfo();
        mainInfo.setTemp(10.0);
        mainInfo.setFeelsLike(8.0);
        mainInfo.setHumidity(55);

        WeatherResponse.Weather weatherDetail = new WeatherResponse.Weather();
        weatherDetail.setDescription("clear sky");
        weatherDetail.setIcon("01d");

        WeatherResponse fakeWeather = new WeatherResponse();
        fakeWeather.setMain(mainInfo);
        fakeWeather.setWeather(List.of(weatherDetail));

        when(weatherApiClient.fetchCurrentWeatherByCoordinates(
                eq(String.valueOf(existingLocationEntity.getLatitude())),
                eq(String.valueOf(existingLocationEntity.getLongitude()))
        )).thenReturn(fakeWeather);

        List<LocationWeatherDto> result = locationService.getLocationsWithWeather(1L);

        assertThat(result).hasSize(1);
        LocationWeatherDto dto = result.get(0);

        assertThat(dto.getId()).isEqualTo(100L);
        assertThat(dto.getName()).isEqualTo("TestCity");
        assertThat(dto.getUserId()).isEqualTo(1L);
        assertThat(dto.getLat()).isEqualTo(55.75);
        assertThat(dto.getLon()).isEqualTo(37.61);
        assertThat(dto.getTemp()).isEqualTo(10.0);
        assertThat(dto.getFeelsLike()).isEqualTo(8.0);
        assertThat(dto.getDescription()).isEqualTo("clear sky");
        assertThat(dto.getHumidity()).isEqualTo(55);
        assertThat(dto.getIconUrl()).isEqualTo(String.format("%s%s@2x.png", iconBaseUrl, "01d"));
    }

    @Test
    void removeLocation_ValidLatLon_ExistingRecord_ReturnsTrue() {
        when(locationRepository.deleteByUserIdAndLatitudeAndLongitude(1L, 55.75, 37.61))
                .thenReturn(1L);

        boolean deleted = locationService.removeLocation(1L, "55.75", "37.61");
        assertThat(deleted).isTrue();
    }

    @Test
    void removeLocation_ValidLatLon_NoRecord_ReturnsFalse() {
        when(locationRepository.deleteByUserIdAndLatitudeAndLongitude(1L, 55.75, 37.61))
                .thenReturn(0L);

        boolean deleted = locationService.removeLocation(1L, "55.75", "37.61");
        assertThat(deleted).isFalse();
    }

    @Test
    void removeLocation_InvalidLatLonFormat_ReturnsFalse() {
        boolean deleted = locationService.removeLocation(1L, "not_a_number", "37.61");
        assertThat(deleted).isFalse();
        verify(locationRepository, never())
                .deleteByUserIdAndLatitudeAndLongitude(anyLong(), anyDouble(), anyDouble());
    }
}
