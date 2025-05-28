package org.kivislime.weather.mapper;

import org.kivislime.weather.dto.LocationDto;
import org.kivislime.weather.dto.LocationRegistrationDto;
import org.kivislime.weather.entity.Location;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LocationMapper {
    Location toEntity(LocationRegistrationDto dto);

    @Mapping(target = "userId", source = "entity.user.id")
    LocationDto toDto(Location entity, Double temperature);
}
