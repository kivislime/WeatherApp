package org.kivislime.weather.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kivislime.weather.client.GeocodingResponse;
import org.kivislime.weather.client.WeatherApiClient;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WeatherApiClient weatherApiClient;

    @Mock
    private WeatherMapper weatherMapper;

    @Mock
    private WeatherCacheService weatherCacheService;

    @Mock
    private LocationPersistenceService locationPersistenceService;

    private LocationService locationService;

    @BeforeEach
    void setUp() {
        ExecutorService sameThreadExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });

        locationService = new LocationService(
                locationRepository,
                userRepository,
                weatherApiClient,
                weatherMapper,
                weatherCacheService,
                sameThreadExecutor,
                locationPersistenceService
        );
    }

    @Test
    void addLocation_UserNotFound_ShouldThrowUserNotFoundException() {
        when(userRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> locationService.addLocation(5L, "NonexistentCity"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("id: 5");

        verify(weatherApiClient, never()).fetchCurrentWeatherByName(anyString());
        verify(locationPersistenceService, never()).getOrCreate(any(), anyString(), anyDouble(), anyDouble(), anyDouble());
    }

    @Test
    void addLocation_UserExists_ShouldDelegateToPersistence() {
        User user = new User();
        user.setId(7L);
        user.setLogin("bob");
        user.setPassword("pass");

        when(userRepository.findById(7L)).thenReturn(Optional.of(user));

        WeatherResponse.MainInfo mainInfo = new WeatherResponse.MainInfo();
        mainInfo.setTemp(12.5);
        mainInfo.setFeelsLike(11.0);
        mainInfo.setHumidity(60);

        WeatherResponse.Weather weatherDetail = new WeatherResponse.Weather();
        weatherDetail.setDescription("sunny");
        weatherDetail.setIcon("sun");

        WeatherResponse fakeWeather = new WeatherResponse();
        WeatherResponse.Coordinate coordinate = new WeatherResponse.Coordinate();
        coordinate.setLat(33.0);
        coordinate.setLon(44.0);
        fakeWeather.setCoord(coordinate);
        fakeWeather.setMain(mainInfo);
        fakeWeather.setWeather(List.of(weatherDetail));

        when(weatherApiClient.fetchCurrentWeatherByName("CityX")).thenReturn(fakeWeather);

        LocationDto expectedDto = new LocationDto(200L, "CityX", 7L, 33.0, 44.0, 12.5);
        when(locationPersistenceService.getOrCreate(
                eq(user), eq("CityX"), eq(33.0), eq(44.0), eq(12.5)
        )).thenReturn(expectedDto);

        LocationDto result = locationService.addLocation(7L, "CityX");

        assertThat(result).isEqualTo(expectedDto);

        verify(weatherApiClient).fetchCurrentWeatherByName("CityX");
        verify(locationPersistenceService).getOrCreate(user, "CityX", 33.0, 44.0, 12.5);
    }

    @Test
    void getLocationsWithWeather_NoLocations_ReturnsEmptyList() {
        when(locationRepository.findByUserId(3L)).thenReturn(Collections.emptyList());

        List<LocationWeatherDto> result = locationService.getLocationsWithWeather(3L);

        assertThat(result).isEmpty();
        verify(weatherCacheService, never()).fetchWeather(any());
    }

    @Test
    void getLocationsWithWeather_OneLocation_ShouldReturnDtoList() {
        Location loc = new Location();
        loc.setId(50L);
        loc.setName("Loc50");
        loc.setLatitude(10.1);
        loc.setLongitude(20.2);
        User u = new User();
        u.setId(9L);
        loc.setUser(u);

        when(locationRepository.findByUserId(9L)).thenReturn(List.of(loc));

        String iconBaseUrl = "http://example.com/icons/";
        LocationWeatherDto fakeDto = new LocationWeatherDto(
                50L, "Loc50", 9L, 10.1, 20.2,
                5.5, 4.4, "cloudy", 80, iconBaseUrl + "cloud@2x.png"
        );
        when(weatherCacheService.fetchWeather(loc)).thenReturn(fakeDto);

        List<LocationWeatherDto> result = locationService.getLocationsWithWeather(9L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(fakeDto);

        verify(weatherCacheService).fetchWeather(loc);
    }

    @Test
    void removeLocation_ExistingRecord_ReturnsTrue() {
        when(locationRepository.deleteByUserIdAndLatitudeAndLongitude(4L, 11.0, 22.0)).thenReturn(1L);

        boolean deleted = locationService.removeLocation(4L, 11.0, 22.0);

        assertThat(deleted).isTrue();
        verify(locationRepository).deleteByUserIdAndLatitudeAndLongitude(4L, 11.0, 22.0);
    }

    @Test
    void removeLocation_NoRecord_ReturnsFalse() {
        when(locationRepository.deleteByUserIdAndLatitudeAndLongitude(4L, 11.0, 22.0)).thenReturn(0L);

        boolean deleted = locationService.removeLocation(4L, 11.0, 22.0);

        assertThat(deleted).isFalse();
        verify(locationRepository).deleteByUserIdAndLatitudeAndLongitude(4L, 11.0, 22.0);
    }

    @Test
    void searchLocationsListByName_ShouldMapGeocodingResponseToDto() {
        GeocodingResponse gr1 = new GeocodingResponse();
        gr1.setName("CityA");
        gr1.setLat(12.3);
        gr1.setLon(45.6);
        gr1.setCountry("N/A");
        gr1.setState("N/A");
        GeocodingResponse gr2 = new GeocodingResponse();
        gr2.setName("CityB");
        gr2.setLat(23.4);
        gr2.setLon(56.7);
        gr2.setCountry("N/A");
        gr2.setState("N/A");
        when(weatherApiClient.fetchCurrentGeocodingByName("Query")).thenReturn(List.of(gr1, gr2));

        LocationSearchResultDto dto1 = new LocationSearchResultDto("CityA", 12.3, 45.6, "N/A", "N/A");
        LocationSearchResultDto dto2 = new LocationSearchResultDto("CityB", 23.4, 56.7, "N/A", "N/A");
        when(weatherMapper.toDto(gr1)).thenReturn(dto1);
        when(weatherMapper.toDto(gr2)).thenReturn(dto2);

        List<LocationSearchResultDto> result = locationService.searchLocationsListByName("Query");

        assertThat(result).containsExactly(dto1, dto2);
        verify(weatherApiClient).fetchCurrentGeocodingByName("Query");
        verify(weatherMapper).toDto(gr1);
        verify(weatherMapper).toDto(gr2);
    }
}
