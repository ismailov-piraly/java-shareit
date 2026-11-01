package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ErrorHandler;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(ErrorHandler.class)
class UserControllerMockMvcTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void shouldCreateUser() throws Exception {
        when(userService.addUser(any())).thenReturn(UserDto.builder().id(1L).name("Анна").email("anna@example.com").build());

        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Анна\",\"email\":\"anna@example.com\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.name").value("Анна")).andExpect(jsonPath("$.email").value("anna@example.com"));
    }

    @Test
    void shouldUpdateUser() throws Exception {
        when(userService.updateUser(eq(1L), any())).thenReturn(UserDto.builder().id(1L).name("Новое имя").email("anna@example.com").build());

        mockMvc.perform(patch("/users/1").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Новое имя\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Новое имя"));
    }

    @Test
    void shouldReturnUserById() throws Exception {
        when(userService.getUserById(1L)).thenReturn(UserDto.builder().id(1L).name("Анна").email("anna@example.com").build());

        mockMvc.perform(get("/users/1")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldReturnAllUsers() throws Exception {
        when(userService.getUsers()).thenReturn(List.of(UserDto.builder().id(1L).name("Анна").email("anna@example.com").build()));

        mockMvc.perform(get("/users")).andExpect(status().isOk()).andExpect(jsonPath("$[0].name").value("Анна"));
    }

    @Test
    void shouldDeleteUser() throws Exception {
        mockMvc.perform(delete("/users/1")).andExpect(status().isOk());
        verify(userService).deleteUser(1L);
    }
}
