package org.kivislime.weather.mapper;

import org.kivislime.weather.dto.SessionDto;
import org.kivislime.weather.entity.Session;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SessionMapper {
    SessionDto toSessionDto(Session session);
}
