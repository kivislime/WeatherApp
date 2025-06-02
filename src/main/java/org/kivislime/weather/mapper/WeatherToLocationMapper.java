package org.kivislime.weather.mapper;

import org.kivislime.weather.client.WeatherResponse;
import org.kivislime.weather.dto.LocationRegistrationDto;
import org.kivislime.weather.dto.LocationWeatherDto;
import org.kivislime.weather.entity.Location;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WeatherToLocationMapper {
    @Mapping(source = "coord.lat", target = "latitude")
    @Mapping(source = "coord.lon", target = "longitude")
    @Mapping(source = "name",     target = "name")
    LocationRegistrationDto toRegistrationDto(WeatherResponse weatherResponse);
}
