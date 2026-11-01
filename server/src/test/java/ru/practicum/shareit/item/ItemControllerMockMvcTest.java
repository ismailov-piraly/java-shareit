package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ErrorHandler;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
@Import(ErrorHandler.class)
class ItemControllerMockMvcTest {
    @Autowired
    MockMvc mvc;
    @MockBean
    ItemService service;

    @Test
    void createItem() throws Exception {
        when(service.addItem(eq(1L), any())).thenReturn(ItemDto.builder().id(10L).name("Дрель").description("Для ремонта").available(true).build());
        mvc.perform(post("/items").header("X-Sharer-User-Id", 1).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Дрель\",\"description\":\"Для ремонта\",\"available\":true}")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(10)).andExpect(jsonPath("$.name").value("Дрель"));
    }

    @Test
    void updateItem() throws Exception {
        when(service.updateItem(eq(1L), eq(10L), any())).thenReturn(ItemDto.builder().id(10L).name("Шуруповёрт").build());
        mvc.perform(patch("/items/10").header("X-Sharer-User-Id", 1).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Шуруповёрт\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Шуруповёрт"));
    }

    @Test
    void getItem() throws Exception {
        when(service.getItemById(10L)).thenReturn(ItemDto.builder().id(10L).name("Дрель").build());
        mvc.perform(get("/items/10")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void getOwnerItems() throws Exception {
        when(service.getItemsByOwner(1L)).thenReturn(List.of(ItemDto.builder().id(10L).name("Дрель").build()));
        mvc.perform(get("/items").header("X-Sharer-User-Id", 1)).andExpect(status().isOk()).andExpect(jsonPath("$[0].name").value("Дрель"));
    }

    @Test
    void searchItems() throws Exception {
        when(service.searchItems("дрель")).thenReturn(List.of(ItemDto.builder().id(10L).name("Дрель").build()));
        mvc.perform(get("/items/search").param("text", "дрель")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(10));
    }

    @Test
    void addComment() throws Exception {
        when(service.addComment(2L, 10L, "Отлично")).thenReturn(new ru.practicum.shareit.item.dto.CommentDto(5L, "Отлично", "Покупатель", java.time.LocalDateTime.of(2026, 1, 1, 10, 0)));
        mvc.perform(post("/items/10/comment").header("X-Sharer-User-Id", 2).contentType(MediaType.APPLICATION_JSON).content("{\"text\":\"Отлично\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.text").value("Отлично"));
    }
}
