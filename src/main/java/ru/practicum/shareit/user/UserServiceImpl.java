package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto addUser(UserDto dto) {
        validateEmail(dto.getEmail());
        if (userRepository.existsByEmailIgnoreCase(dto.getEmail())) {
            throw new ConflictException("Пользователь с таким email уже существует");
        }
        String name = dto.getName() == null || dto.getName().isBlank() ? dto.getEmail() : dto.getName();
        User user = User.builder().name(name).email(dto.getEmail()).build();
        log.info("Создание пользователя с email={}", dto.getEmail());
        return UserMapper.toDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserDto updateUser(Long userId, UserDto dto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        if (dto.getEmail() != null) {
            validateEmail(dto.getEmail());
            if (userRepository.existsByEmailIgnoreCaseAndIdNot(dto.getEmail(), userId)) {
                throw new ConflictException("Пользователь с таким email уже существует");
            }
            user.setEmail(dto.getEmail());
        }
        if (dto.getName() != null) {
            if (dto.getName().isBlank()) throw new ValidationException("Имя не может быть пустым");
            user.setName(dto.getName());
        }
        return UserMapper.toDto(userRepository.save(user));
    }

    @Override
    public UserDto getUserById(Long userId) {
        return UserMapper.toDto(userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден")));
    }

    @Override
    public List<UserDto> getUsers() {
        return userRepository.findAll().stream().map(UserMapper::toDto).toList();
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId))
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        userRepository.deleteById(userId);
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) throw new ValidationException("Email не может быть пустым");
        if (!email.contains("@")) throw new ValidationException("Некорректный email");
    }
}
