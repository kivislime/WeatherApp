package org.kivislime.weather;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SessionMapper {
    SessionDto toSessionDto(Session session);
}
