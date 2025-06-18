package org.kivislime.weatherapp.weather.mapper;

import org.kivislime.weatherapp.weather.dto.GeocodingResponse;
import org.kivislime.weatherapp.location.dto.LocationSearchResultDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WeatherMapper {
    LocationSearchResultDto toDto(GeocodingResponse geocodingResponse);
}
