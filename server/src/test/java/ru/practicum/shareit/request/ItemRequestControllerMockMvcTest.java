package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ErrorHandler;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
@Import(ErrorHandler.class)
class ItemRequestControllerMockMvcTest {
    @Autowired
    MockMvc mvc;
    @MockBean
    ItemRequestRepository requests;
    @MockBean
    ItemRepository items;
    @MockBean
    UserRepository users;

    private ItemRequest request() {
        return ItemRequest.builder().id(4L).description("Нужна дрель").requester(User.builder().id(1L).name("Анна").email("anna@example.com").build()).created(LocalDateTime.of(2026, 10, 1, 12, 0)).build();
    }

    @Test
    void createRequest() throws Exception {
        when(users.findById(1L)).thenReturn(Optional.of(request().getRequester()));
        when(requests.save(any())).thenAnswer(inv -> {
            ItemRequest r = inv.getArgument(0);
            r.setId(4L);
            return r;
        });
        when(items.findByRequestId(4L)).thenReturn(List.of());
        mvc.perform(post("/requests").header("X-Sharer-User-Id", 1).contentType(MediaType.APPLICATION_JSON).content("{\"description\":\"Нужна дрель\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(4)).andExpect(jsonPath("$.description").value("Нужна дрель"));
    }

    @Test
    void getOwnRequests() throws Exception {
        when(users.findById(1L)).thenReturn(Optional.of(request().getRequester()));
        when(requests.findByRequesterIdOrderByCreatedDesc(1L)).thenReturn(List.of(request()));
        when(items.findByRequestId(4L)).thenReturn(List.of());
        mvc.perform(get("/requests").header("X-Sharer-User-Id", 1)).andExpect(status().isOk()).andExpect(jsonPath("$[0].description").value("Нужна дрель"));
    }

    @Test
    void getOtherUsersRequests() throws Exception {
        when(users.findById(2L)).thenReturn(Optional.of(User.builder().id(2L).name("Борис").email("b@example.com").build()));
        when(requests.findByRequesterIdNotOrderByCreatedDesc(2L)).thenReturn(List.of(request()));
        when(items.findByRequestId(4L)).thenReturn(List.of());
        mvc.perform(get("/requests/all").header("X-Sharer-User-Id", 2)).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(4));
    }

    @Test
    void getRequestById() throws Exception {
        when(users.findById(2L)).thenReturn(Optional.of(User.builder().id(2L).name("Борис").email("b@example.com").build()));
        when(requests.findById(4L)).thenReturn(Optional.of(request()));
        when(items.findByRequestId(4L)).thenReturn(List.of());
        mvc.perform(get("/requests/4").header("X-Sharer-User-Id", 2)).andExpect(status().isOk()).andExpect(jsonPath("$.description").value("Нужна дрель"));
    }
}
