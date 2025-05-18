package org.kivislime.weather;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LocationMapper {
    Location toEntity(LocationRegistrationDto dto);

    LocationDto toDto(Location entity);
}
