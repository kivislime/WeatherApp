package org.kivislime.weather.mapper;

import org.kivislime.weather.dto.UserDto;
import org.kivislime.weather.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);

    User toEntity(UserDto userDto);
}
