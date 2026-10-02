package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;

public class UserMapper {

    private UserMapper() {
    }

    public static UserDto toDto(User user) {
        return UserDto.builder().id(user.getId()).name(user.getName()).email(user.getEmail()).build();
    }

    public static User toModel(UserDto dto) {
        return User.builder().id(dto.getId()).name(dto.getName()).email(dto.getEmail()).build();
    }
}