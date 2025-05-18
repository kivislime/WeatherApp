package org.kivislime.weather;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface WeatherToLocationMapper {
    @Mapping(source = "coord.lat", target = "latitude")
    @Mapping(source = "coord.lon", target = "longitude")
    @Mapping(source = "name",     target = "name")
    LocationRegistrationDto toRegistrationDto(WeatherResponse weatherResponse);
}
