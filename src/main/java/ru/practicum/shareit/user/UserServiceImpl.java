package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserStorage userStorage;

    @Override
    public UserDto addUser(UserDto userDto) {

        validateUser(userDto);

        if (userStorage.existsByEmail(userDto.getEmail())) {
            throw new ConflictException("Пользователь с таким email уже существует");
        }

        if (userDto.getName() == null || userDto.getName().isBlank()) {
            userDto.setName(userDto.getEmail());
        }

        User user = UserMapper.toModel(userDto);

        return UserMapper.toDto(userStorage.save(user));
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {

        User existingUser = userStorage.findById(userId);

        if (existingUser == null) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        if (userDto.getEmail() != null) {

            if (userDto.getEmail().isBlank() || !userDto.getEmail().contains("@")) {
                throw new ValidationException("Некорректный email");
            }

            if (userStorage.existsByEmailAndNotId(userDto.getEmail(), userId)) {

                throw new ConflictException("Пользователь с таким email уже существует");
            }

            existingUser.setEmail(userDto.getEmail());
        }

        if (userDto.getName() != null) {
            existingUser.setName(userDto.getName());
        }

        userStorage.update(existingUser);

        return UserMapper.toDto(existingUser);
    }

    @Override
    public UserDto getUserById(Long userId) {

        User user = userStorage.findById(userId);

        if (user == null) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        return UserMapper.toDto(user);
    }

    @Override
    public List<UserDto> getUsers() {

        return userStorage.findAll().stream().map(UserMapper::toDto).toList();
    }

    private void validateUser(UserDto userDto) {

        if (userDto.getEmail() == null || userDto.getEmail().isBlank()) {

            throw new ValidationException("Email не может быть пустым");
        }

        if (!userDto.getEmail().contains("@")) {
            throw new ValidationException("Некорректный email");
        }
    }

    @Override
    public void deleteUser(Long userId) {

        if (!userStorage.existsById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        userStorage.delete(userId);
    }
}