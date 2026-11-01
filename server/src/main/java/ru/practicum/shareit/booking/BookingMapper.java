package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.user.UserMapper;

public final class BookingMapper {
    private BookingMapper() {
    }

    public static BookingDto toDto(Booking booking) {
        return BookingDto.builder().id(booking.getId()).start(booking.getStart()).end(booking.getEnd()).item(ItemMapper.toDto(booking.getItem())).booker(UserMapper.toDto(booking.getBooker())).status(booking.getStatus()).build();
    }
}
