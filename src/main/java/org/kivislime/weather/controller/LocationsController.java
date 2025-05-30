package org.kivislime.weather.controller;

import lombok.RequiredArgsConstructor;
import org.kivislime.weather.client.GeocodingResponse;
import org.kivislime.weather.client.WeatherApiClient;
import org.kivislime.weather.client.WeatherResponse;
import org.kivislime.weather.mapper.WeatherToLocationMapper;
import org.kivislime.weather.dto.LocationDto;
import org.kivislime.weather.dto.LocationRegistrationDto;
import org.kivislime.weather.dto.LocationWeatherDto;
import org.kivislime.weather.service.LocationService;
import org.kivislime.weather.service.SessionService;
import org.kivislime.weather.dto.UserDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//TODO: ТЕСТЫ ЗАПИЛИТЬ, УЗНАЧТЬ СНАЧАЛА для каких классов они пиляться, потом сделать ширфование паролей, либо сначала шифрование
@Controller
@RequiredArgsConstructor
public class LocationsController {
    final LocationService locationService;
    final WeatherApiClient weatherApiClient;
    final WeatherToLocationMapper weatherToLocationMapper;

    @GetMapping("/locations")
    public String locations(@RequestAttribute("user") UserDto userDto,
                            Model model) {
        List<LocationWeatherDto> locationWeatherDtos = locationService.getLocationsWithWeather(userDto.getId());
        model.addAttribute("locations", locationWeatherDtos);
        model.addAttribute("userId", userDto.getId());
        //TODO: если размер бд станет слишком большим? Дропать ее мб
        return "index";
    }

    @GetMapping("/locations/search")
    public String searchWeather(
            @RequestParam(name = "name", required = false) String name,
            Model model) {

        if (name != null && !name.isBlank()) {
            List<GeocodingResponse> result = weatherApiClient.fetchCurrentGeocodingByName(name);

            model.addAttribute("searchResults", result);
            model.addAttribute("searchQuery", name);
        }

        return "search-results";
    }

    @PostMapping("/locations/search")
    public String addLocation(@RequestParam(name = "name", required = false) String name,
                              @RequestParam(name = "lat", required = false) String lat,
                              @RequestParam(name = "lon", required = false) String lon,
                              @RequestAttribute("user") UserDto userDto,
                              @CookieValue(name = "SESSION_ID", required = false) String sessionUuid
    ) {
        WeatherResponse weatherResponse;
        if (name != null) {
            weatherResponse = weatherApiClient.fetchCurrentWeatherByName(name);
        } else if (lat != null && lon != null) {
            weatherResponse = weatherApiClient.fetchCurrentWeatherByCoordinates(lat, lon);
        } else {
            return "redirect:/locations/search";
        }
        LocationRegistrationDto locationRegistrationDto = weatherToLocationMapper.toRegistrationDto(weatherResponse);
        //TODO: неиспользуемые переменные удалить? Убрать возвращаемые значения в сервисе?
        LocationDto locationDto = locationService.addLocation(userDto.getId(), weatherResponse.getMain().getTemp(), locationRegistrationDto);
        return "redirect:/locations";
    }

    @PostMapping("/locations/delete")
    public String locationsDelete(@RequestParam("lat") String lat,
                                  @RequestParam("lon") String lon,
                                  @RequestAttribute("user") UserDto userDto) {
        boolean isDeleted = locationService.removeLocation(userDto.getId(), lat, lon);
        return "redirect:/locations";
    }
}
