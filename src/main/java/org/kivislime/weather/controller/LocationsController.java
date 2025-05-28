package org.kivislime.weather.controller;

import lombok.RequiredArgsConstructor;
import org.kivislime.weather.client.GeocodingResponse;
import org.kivislime.weather.client.WeatherApiClient;
import org.kivislime.weather.client.WeatherResponse;
import org.kivislime.weather.mapper.WeatherToLocationMapper;
import org.kivislime.weather.dto.LocationDto;
import org.kivislime.weather.dto.LocationRegistrationDto;
import org.kivislime.weather.dto.LocationWeatherDto;
import org.kivislime.weather.service.LocationsService;
import org.kivislime.weather.service.SessionService;
import org.kivislime.weather.dto.UserDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class LocationsController {
    final LocationsService locationsService;
    final SessionService sessionService;
    final WeatherApiClient weatherApiClient;
    final WeatherToLocationMapper weatherToLocationMapper;

    //TODO: сделать контроллер ошибок, чтобы пересылать ошибки 404 и сделать @ControllerAdvice. Самому без чат гпт го ---------------------------------------
    @GetMapping("/locations")
    public String locations(@RequestAttribute("user") UserDto userDto,
                            Model model) {
        List<LocationWeatherDto> locationWeatherDtos = locationsService.getLocationsWithWeather(userDto.getId());
        model.addAttribute("locations", locationWeatherDtos);
        model.addAttribute("userId", userDto.getId());
        //TODO: "Просмотр списка локаций, для каждой локации отображается название и температура"
        // В ЦИКЛЕ ВОЗВРАЩАТЬ ТЕМПЕРАТУРЫ КАЖДОЙ ЛОКАЦИИ? Закончится лимит так-то
        // ограничить количество локаций на юзера - 5, перед добавлением локации проверять ее наличие в таблице
        // если размер бд станет слишком большим? Дропать ее мб
        return "index";
    }

    //TODO: state = null у некоторых локаций, поч? Отобразить по другому как-то
    @GetMapping("/locations/search")
    public String searchWeather(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "lat", required = false) String lat,
            @RequestParam(name = "lon", required = false) String lon,
            Model model) {

        if (name != null && !name.isBlank()) {

            List<GeocodingResponse> result = weatherApiClient.fetchCurrentGeocodingByName(name);

            model.addAttribute("searchResults", result);
            model.addAttribute("searchQuery", name);
        }

        // Если нужен поиск по координатам, можно оставить прежнюю логику, но для простоты — уберём
        // if (lat != null && lon != null) {
        //     ...
        // }

        return "search-results";
    }

    //TODO: просто передавать id отсюда небезопасно без входа(логин)
    // проверять наличие локации в бд перед добавлением, name - saveOrReturn() ?
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
        LocationDto locationDto = locationsService.addLocation(userDto.getId(), weatherResponse.getMain().getTemp(), locationRegistrationDto);
        //TODO: после добавления результата надо чтобы результаты поиска не скидывались
        return "redirect:/locations";
    }

    @PostMapping("/locations/delete")
    public String locationsDelete(@RequestParam("lat") String lat,
                                  @RequestParam("lon") String lon,
                                  @RequestAttribute("user") UserDto userDto) {
        boolean isDeleted = locationsService.removeLocation(userDto.getId(), lat, lon);
        return "redirect:/locations";
    }
}
