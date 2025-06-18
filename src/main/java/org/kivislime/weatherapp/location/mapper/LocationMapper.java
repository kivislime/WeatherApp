package org.kivislime.weatherapp.location.mapper;

import org.kivislime.weatherapp.location.entity.Location;
import org.kivislime.weatherapp.location.dto.LocationDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LocationMapper {
    @Mapping(target = "userId", source = "entity.user.id")
    LocationDto toDto(Location entity, Double temperature);
}
