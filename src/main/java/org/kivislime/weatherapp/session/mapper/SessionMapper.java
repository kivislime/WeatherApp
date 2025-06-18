package org.kivislime.weatherapp.session.mapper;

import org.kivislime.weatherapp.session.dto.SessionDto;
import org.kivislime.weatherapp.session.entity.Session;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SessionMapper {
    SessionDto toDto(Session session);
}
