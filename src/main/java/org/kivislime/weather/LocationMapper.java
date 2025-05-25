package org.kivislime.weather;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LocationMapper {
    Location toEntity(LocationRegistrationDto dto);

    @Mapping(target = "userId", source = "entity.user.id")
    LocationDto toDto(Location entity, Double temperature);
}
