package org.kivislime.weatherapp.user.mapper;

import org.kivislime.weatherapp.user.dto.UserDto;
import org.kivislime.weatherapp.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);
}
