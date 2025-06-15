package org.kivislime.weather.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kivislime.weather.dto.LocationSearchResultDto;
import org.kivislime.weather.dto.LocationWeatherDto;
import org.kivislime.weather.dto.UserDto;
import org.kivislime.weather.service.LocationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LocationsController {
    private final LocationService locationService;

    @GetMapping("/locations")
    public String locations(@RequestAttribute("user") UserDto userDto,
                            Model model) {
        List<LocationWeatherDto> locationWeatherDtos = locationService.getLocationsWithWeather(userDto.getId());
        model.addAttribute("locations", locationWeatherDtos);
        model.addAttribute("userId", userDto.getId());
        return "index";
    }

    @GetMapping("/locations/search")    
    public String searchWeather(@RequestParam(name = "name", required = false) String name,
                                Model model) {
        if (name != null && !name.isBlank()) {
            List<LocationSearchResultDto> result = locationService.searchLocationsListByName(name);
            model.addAttribute("searchResults", result);
            model.addAttribute("searchQuery", name);
        }
        return "search-results";
    }

    @PostMapping("/locations/search")
    public String addLocation(@RequestParam(name = "name", required = false) String name,
                              @RequestAttribute("user") UserDto userDto) {
        if (name == null || name.isBlank()) {
            return "redirect:/locations/search";
        }
        locationService.addLocation(userDto.getId(), name);
        return "redirect:/locations";
    }

    @PostMapping("/locations/delete")
    public String deleteLocation(@RequestParam("lat") String lat,
                                 @RequestParam("lon") String lon,
                                 @RequestAttribute("user") UserDto userDto,
                                 RedirectAttributes ra) {
        Double parseLat;
        Double parseLon;
        try {
            parseLat = Double.parseDouble(lat);
            parseLon = Double.parseDouble(lon);
        } catch (NumberFormatException e) {
            ra.addFlashAttribute("deleteError", "Incorrect format lat/lon");
            log.error("Incorrect format lat/lon from user {}: lat='{}', lon='{}'", userDto.getLogin(), lat, lon);
            return "redirect:/locations";
        }

        boolean isDeleted = locationService.removeLocation(userDto.getId(), parseLat, parseLon);
        if (!isDeleted) {
            ra.addFlashAttribute("deleteError", "Location not found or already deleted");
            log.error("Attempt to delete non‐existent location from user {}: lat={}, lon={}",
                    userDto.getLogin(), parseLat, parseLon);
        }
        return "redirect:/locations";
    }
}
