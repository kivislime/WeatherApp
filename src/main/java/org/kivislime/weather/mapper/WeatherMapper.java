package org.kivislime.weather.mapper;

import org.kivislime.weather.client.GeocodingResponse;
import org.kivislime.weather.dto.LocationSearchResultDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WeatherMapper {
    LocationSearchResultDto toDto(GeocodingResponse geocodingResponse);
}
