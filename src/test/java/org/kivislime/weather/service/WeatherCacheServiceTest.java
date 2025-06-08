package org.kivislime.weather.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kivislime.weather.client.WeatherApiClient;
import org.kivislime.weather.client.WeatherResponse;
import org.kivislime.weather.dto.LocationWeatherDto;
import org.kivislime.weather.entity.Location;
import org.kivislime.weather.entity.User;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeatherCacheServiceTest {

    @Mock
    private WeatherApiClient weatherApiClient;

    private final String iconBaseUrl = "http://openweathermap.org/img/";

    private WeatherCacheService service;

    @BeforeEach
    void setUp() {
        service = new WeatherCacheService(weatherApiClient, iconBaseUrl);
    }

    @Test
    void fetchWeather_WhenWeatherListNonEmpty_ShouldMapAllFieldsCorrectly() {
        User fakeUser = new User();
        fakeUser.setId(5L);

        Location loc = new Location();
        loc.setId(99L);
        loc.setName("TestCity");
        loc.setLatitude(12.34);
        loc.setLongitude(56.78);
        loc.setUser(fakeUser);

        WeatherResponse.MainInfo main = new WeatherResponse.MainInfo();
        main.setTemp(18.5);
        main.setFeelsLike(17.0);
        main.setHumidity(70);

        WeatherResponse.Weather w = new WeatherResponse.Weather();
        w.setDescription("clear sky");
        w.setIcon("01d");

        WeatherResponse fakeResponse = new WeatherResponse();
        fakeResponse.setMain(main);
        fakeResponse.setWeather(List.of(w));

        when(weatherApiClient.fetchCurrentWeatherByCoordinates(
                eq("12.34"), eq("56.78")
        )).thenReturn(fakeResponse);

        LocationWeatherDto dto = service.fetchWeather(loc);

        assertThat(dto.getId()).isEqualTo(99L);
        assertThat(dto.getName()).isEqualTo("TestCity");
        assertThat(dto.getUserId()).isEqualTo(5L);
        assertThat(dto.getLat()).isEqualTo(12.34);
        assertThat(dto.getLon()).isEqualTo(56.78);

        assertThat(dto.getTemp()).isEqualTo(18.5);
        assertThat(dto.getFeelsLike()).isEqualTo(17.0);
        assertThat(dto.getHumidity()).isEqualTo(70);
        assertThat(dto.getDescription()).isEqualTo("clear sky");
        assertThat(dto.getIconUrl())
                .isEqualTo(iconBaseUrl + "01d@2x.png");

        verify(weatherApiClient).fetchCurrentWeatherByCoordinates("12.34", "56.78");
    }

    @Test
    void fetchWeather_WhenWeatherListEmpty_ShouldUseUnknownIconAndEmptyDescription() {
        User fakeUser = new User();
        fakeUser.setId(3L);

        Location loc = new Location();
        loc.setId(77L);
        loc.setName("EmptyCity");
        loc.setLatitude(1.1);
        loc.setLongitude(2.2);
        loc.setUser(fakeUser);

        WeatherResponse.MainInfo main = new WeatherResponse.MainInfo();
        main.setTemp(5.5);
        main.setFeelsLike(5.0);
        main.setHumidity(50);

        WeatherResponse fakeResponse = new WeatherResponse();
        fakeResponse.setMain(main);
        fakeResponse.setWeather(Collections.emptyList());

        when(weatherApiClient.fetchCurrentWeatherByCoordinates(
                eq("1.1"), eq("2.2")
        )).thenReturn(fakeResponse);

        LocationWeatherDto dto = service.fetchWeather(loc);

        assertThat(dto.getTemp()).isEqualTo(5.5);
        assertThat(dto.getFeelsLike()).isEqualTo(5.0);
        assertThat(dto.getHumidity()).isEqualTo(50);
        assertThat(dto.getDescription()).isEmpty();
        assertThat(dto.getIconUrl()).isEqualTo(iconBaseUrl + "unknown@2x.png");
        verify(weatherApiClient).fetchCurrentWeatherByCoordinates("1.1", "2.2");
    }
}
