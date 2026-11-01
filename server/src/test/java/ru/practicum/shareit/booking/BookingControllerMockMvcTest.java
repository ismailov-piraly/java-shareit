package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.ErrorHandler;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
@Import(ErrorHandler.class)
class BookingControllerMockMvcTest {
    @Autowired
    MockMvc mvc;
    @MockBean
    BookingService service;

    private BookingDto dto() {
        return BookingDto.builder().id(3L).start(LocalDateTime.of(2026, 11, 1, 10, 0)).end(LocalDateTime.of(2026, 11, 2, 10, 0)).item(ItemDto.builder().id(7L).name("Дрель").build()).booker(UserDto.builder().id(2L).name("Покупатель").email("buyer@example.com").build()).status(BookingStatus.WAITING).build();
    }

    @Test
    void createBooking() throws Exception {
        when(service.create(eq(2L), any())).thenReturn(dto());
        mvc.perform(post("/bookings").header("X-Sharer-User-Id", 2).contentType(MediaType.APPLICATION_JSON).content("{\"itemId\":7,\"start\":\"2026-11-01T10:00:00\",\"end\":\"2026-11-02T10:00:00\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(3)).andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void approveBooking() throws Exception {
        BookingDto approved = dto();
        approved.setStatus(BookingStatus.APPROVED);
        when(service.approve(1L, 3L, true)).thenReturn(approved);
        mvc.perform(patch("/bookings/3").header("X-Sharer-User-Id", 1).param("approved", "true")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void getBooking() throws Exception {
        when(service.getById(2L, 3L)).thenReturn(dto());
        mvc.perform(get("/bookings/3").header("X-Sharer-User-Id", 2)).andExpect(status().isOk()).andExpect(jsonPath("$.item.id").value(7));
    }

    @Test
    void getBookerBookings() throws Exception {
        when(service.getByBooker(2L, BookingState.ALL)).thenReturn(List.of(dto()));
        mvc.perform(get("/bookings").header("X-Sharer-User-Id", 2)).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(3));
    }

    @Test
    void getOwnerBookings() throws Exception {
        when(service.getByOwner(1L, BookingState.ALL)).thenReturn(List.of(dto()));
        mvc.perform(get("/bookings/owner").header("X-Sharer-User-Id", 1)).andExpect(status().isOk()).andExpect(jsonPath("$[0].status").value("WAITING"));
    }
}
