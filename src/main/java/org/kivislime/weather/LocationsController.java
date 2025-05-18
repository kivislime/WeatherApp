package org.kivislime.weather;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

//TODO: Определить передаваемые параметры openWeather и далее от них отталкиваться
// узнать про возможность отсылки страницы со списком при нажатии на которую идет запрос уже на другой соответсвующий эндпоинт
// с уже заданными параметрами
//TODO: после предыдущего todo доделать остальные эндпоинты. Post эндпоинт
@Controller
public class LocationsController {
    final LocationsService locationsService;
    final WeatherApiClient weatherApiClient;
    final WeatherToLocationMapper weatherToLocationMapper;

    public LocationsController(LocationsService locationsService, WeatherApiClient weatherApiClient, WeatherToLocationMapper weatherToLocationMapper) {
        this.weatherApiClient = weatherApiClient;
        this.weatherToLocationMapper = weatherToLocationMapper;
        this.locationsService = locationsService;
    }


    @GetMapping("/locations")
    @ResponseBody
    public String locations(@RequestParam("lat") String lat, @RequestParam("lon") String lon) {
        return weatherApiClient.fetchCurrentWeather(lat, lon);
    }

    @PostMapping("/locations")
    @ResponseBody
    public String locationsPost(@RequestParam("lat") String lat, @RequestParam("lon") String lon, @RequestParam("id") Long id) {
        String weatherResponse = weatherApiClient.fetchCurrentWeather(lat, lon);
        WeatherResponse weather = JacksonUtil.toWeatherResponse(weatherResponse);
        //TODO: user_id связан с куки? Тип не указыать? не пон поч

        LocationRegistrationDto location = weatherToLocationMapper.toRegistrationDto(weather);
        LocationDto LocationDto = locationsService.addLocation(location, id);

        return JacksonUtil.toJson(LocationDto);
    }
}
