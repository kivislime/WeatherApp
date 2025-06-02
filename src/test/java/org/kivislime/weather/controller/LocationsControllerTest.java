package org.kivislime.weather.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kivislime.weather.client.GeocodingResponse;
import org.kivislime.weather.client.WeatherApiClient;
import org.kivislime.weather.client.WeatherResponse;
import org.kivislime.weather.dto.LocationDto;
import org.kivislime.weather.dto.LocationRegistrationDto;
import org.kivislime.weather.dto.LocationWeatherDto;
import org.kivislime.weather.dto.UserDto;
import org.kivislime.weather.mapper.WeatherToLocationMapper;
import org.kivislime.weather.service.LocationService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import jakarta.servlet.http.Cookie;  // <–– вот этот импорт нужен

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class LocationsControllerTest {

    @Mock
    private LocationService locationService;

    @Mock
    private WeatherApiClient weatherApiClient;

    @Mock
    private WeatherToLocationMapper weatherToLocationMapper;

    @InjectMocks
    private LocationsController locationsController;

    private MockMvc mockMvc;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(locationsController).build();
        userDto = new UserDto(42L, "alice");
    }


    @Test
    void getLocations_ShouldPopulateModelAndReturnIndexView() throws Exception {
        List<LocationWeatherDto> mockList = List.of(
                new LocationWeatherDto(1L, "City", 42L, 10.0, 20.0, 25.0, 23.0, "clear", 50, "icon.png")
        );

        when(locationService.getLocationsWithWeather(42L)).thenReturn(mockList);

        mockMvc.perform(get("/locations")
                        .requestAttr("user", userDto))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("locations", mockList))
                .andExpect(model().attribute("userId", 42L));

        verify(locationService, times(1)).getLocationsWithWeather(42L);
    }

    @Test
    void searchWithName_ShouldPopulateResultsAndReturnSearchResultsView() throws Exception {
        List<GeocodingResponse> geocodingList = List.of(new GeocodingResponse());
        when(weatherApiClient.fetchCurrentGeocodingByName("Paris")).thenReturn(geocodingList);

        mockMvc.perform(get("/locations/search")
                        .param("name", "Paris"))
                .andExpect(status().isOk())
                .andExpect(view().name("search-results"))
                .andExpect(model().attribute("searchResults", geocodingList))
                .andExpect(model().attribute("searchQuery", "Paris"));

        verify(weatherApiClient, times(1)).fetchCurrentGeocodingByName("Paris");
    }

    @Test
    void searchWithoutName_ShouldReturnSearchResultsViewWithoutResults() throws Exception {
        mockMvc.perform(get("/locations/search"))
                .andExpect(status().isOk())
                .andExpect(view().name("search-results"))
                .andExpect(model().attributeDoesNotExist("searchResults", "searchQuery"));

        verify(weatherApiClient, never()).fetchCurrentGeocodingByName(anyString());
    }

    @Test
    void postSearch_WithName_ShouldAddLocationAndRedirect() throws Exception {
        WeatherResponse weatherResponse = new WeatherResponse();
        weatherResponse.setMain(new WeatherResponse.MainInfo());
        weatherResponse.getMain().setTemp(15.0);
        when(weatherApiClient.fetchCurrentWeatherByName("Berlin")).thenReturn(weatherResponse);

        LocationRegistrationDto regDto = new LocationRegistrationDto("Berlin", 0.0, 0.0);
        when(weatherToLocationMapper.toRegistrationDto(weatherResponse)).thenReturn(regDto);

        LocationDto locationDto = new LocationDto(1L, "Berlin", 42L, 0.0, 0.0, 15.0);
        when(locationService.addLocation(eq(42L), eq(15.0), eq(regDto))).thenReturn(locationDto);

        mockMvc.perform(post("/locations/search")
                        .param("name", "Berlin")
                        .requestAttr("user", userDto)
                        .cookie(new Cookie("SESSION_ID", "dummy")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/locations"));

        verify(weatherApiClient, times(1)).fetchCurrentWeatherByName("Berlin");
        verify(locationService, times(1)).addLocation(42L, 15.0, regDto);
    }

    @Test
    void postSearch_WithLatLon_ShouldAddLocationAndRedirect() throws Exception {
        WeatherResponse weatherResponse = new WeatherResponse();
        weatherResponse.setMain(new WeatherResponse.MainInfo());
        weatherResponse.getMain().setTemp(20.0);
        when(weatherApiClient.fetchCurrentWeatherByCoordinates("10", "20")).thenReturn(weatherResponse);

        LocationRegistrationDto regDto = new LocationRegistrationDto("City", 10.0, 20.0);
        when(weatherToLocationMapper.toRegistrationDto(weatherResponse)).thenReturn(regDto);

        LocationDto locationDto = new LocationDto(2L, "City", 42L, 10.0, 20.0, 20.0);
        when(locationService.addLocation(eq(42L), eq(20.0), eq(regDto))).thenReturn(locationDto);

        mockMvc.perform(post("/locations/search")
                        .param("lat", "10")
                        .param("lon", "20")
                        .requestAttr("user", userDto)
                        .cookie(new Cookie("SESSION_ID", "dummy")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/locations"));

        verify(weatherApiClient, times(1)).fetchCurrentWeatherByCoordinates("10", "20");
        verify(locationService, times(1)).addLocation(42L, 20.0, regDto);
    }

    @Test
    void postSearch_WithoutParams_ShouldRedirectToSearch() throws Exception {
        mockMvc.perform(post("/locations/search")
                        .requestAttr("user", userDto))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/locations/search"));

        verifyNoInteractions(weatherApiClient);
        verifyNoInteractions(locationService);
    }

    @Test
    void postDelete_ShouldRemoveLocationAndRedirect() throws Exception {
        when(locationService.removeLocation(42L, "10", "20")).thenReturn(true);

        mockMvc.perform(post("/locations/delete")
                        .param("lat", "10")
                        .param("lon", "20")
                        .requestAttr("user", userDto))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/locations"));

        verify(locationService, times(1)).removeLocation(42L, "10", "20");
    }
}
