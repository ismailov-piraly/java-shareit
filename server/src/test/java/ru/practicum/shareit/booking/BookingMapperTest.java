package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BookingMapperTest {
    @Test
    void mapsBookingToDto() {
        User owner = User.builder().id(1L).name("Владелец").email("owner@example.com").build();
        User booker = User.builder().id(2L).name("Арендатор").email("booker@example.com").build();
        Item item = Item.builder().id(3L).name("Дрель").description("Описание").available(true).owner(owner).build();
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(1);
        Booking booking = Booking.builder().id(4L).start(start).end(end).item(item).booker(booker).status(BookingStatus.WAITING).build();

        var dto = BookingMapper.toDto(booking);

        assertEquals(4L, dto.getId());
        assertEquals(start, dto.getStart());
        assertEquals(end, dto.getEnd());
        assertEquals(3L, dto.getItem().getId());
        assertEquals(2L, dto.getBooker().getId());
        assertEquals(BookingStatus.WAITING, dto.getStatus());
    }
}
