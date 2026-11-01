package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceIntegrationTest {
    @Autowired
    private UserService userService;

    @Test
    void shouldCreateAndFindUser() {
        UserDto created = userService.addUser(UserDto.builder().name("Тестовый пользователь").email("test@example.com").build());

        UserDto found = userService.getUserById(created.getId());

        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getName()).isEqualTo("Тестовый пользователь");
        assertThat(found.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void shouldUseEmailAsNameWhenNameIsMissing() {
        UserDto created = userService.addUser(UserDto.builder().email("noname@example.com").build());

        assertThat(created.getName()).isEqualTo("noname@example.com");
    }

    @Test
    void shouldUpdateUserFields() {
        UserDto created = userService.addUser(UserDto.builder().name("Старое имя").email("update@example.com").build());

        UserDto updated = userService.updateUser(created.getId(), UserDto.builder().name("Новое имя").email("new@example.com").build());

        assertThat(updated.getName()).isEqualTo("Новое имя");
        assertThat(updated.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void shouldRejectDuplicateEmail() {
        userService.addUser(UserDto.builder().name("Первый").email("same@example.com").build());

        assertThrows(ConflictException.class, () -> userService.addUser(UserDto.builder().name("Второй").email("SAME@example.com").build()));
    }

    @Test
    void shouldRejectInvalidEmail() {
        assertThrows(ValidationException.class, () -> userService.addUser(UserDto.builder().name("Пользователь").email("invalid-email").build()));
    }

    @Test
    void shouldThrowWhenUserDoesNotExist() {
        assertThrows(NotFoundException.class, () -> userService.getUserById(-1L));
    }

    @Test
    void shouldReturnAllUsersAndDeleteUser() {
        UserDto created = userService.addUser(UserDto.builder().name("Удаляемый").email("delete@example.com").build());

        assertThat(userService.getUsers()).hasSize(1);
        userService.deleteUser(created.getId());

        assertThat(userService.getUsers()).isEmpty();
        assertThrows(NotFoundException.class, () -> userService.deleteUser(created.getId()));
    }

    @Test
    void shouldRejectBlankEmailAndBlankName() {
        assertThrows(ValidationException.class, () -> userService.addUser(UserDto.builder().email(" ").name("Имя").build()));

        UserDto created = userService.addUser(UserDto.builder().email("blankname@example.com").build());
        assertThrows(ValidationException.class, () -> userService.updateUser(created.getId(), UserDto.builder().name(" ").build()));
    }

    @Test
    void shouldRejectDuplicateEmailOnUpdateAndAllowPartialUpdate() {
        UserDto first = userService.addUser(UserDto.builder().name("Первый").email("first@example.com").build());
        userService.addUser(UserDto.builder().name("Второй").email("second@example.com").build());

        assertThrows(ConflictException.class, () -> userService.updateUser(first.getId(), UserDto.builder().email("second@example.com").build()));

        UserDto updated = userService.updateUser(first.getId(), UserDto.builder().name("Новое имя").build());
        assertThat(updated.getName()).isEqualTo("Новое имя");
        assertThat(updated.getEmail()).isEqualTo("first@example.com");
    }

    @Test
    void shouldRejectUpdateForMissingUser() {
        assertThrows(NotFoundException.class, () -> userService.updateUser(-1L, UserDto.builder().name("Имя").build()));
    }
}
