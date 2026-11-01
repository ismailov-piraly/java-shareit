package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @InjectMocks
    private BookingServiceImpl service;

    private User user(Long id) {
        return User.builder().id(id).name("User" + id).email("user" + id + "@example.com").build();
    }

    private Item item() {
        return Item.builder().id(10L).name("Дрель").description("Для ремонта").available(true).owner(user(1L)).build();
    }

    private Booking booking(BookingStatus status, LocalDateTime start, LocalDateTime end) {
        return Booking.builder().id(5L).start(start).end(end).item(item()).booker(user(2L)).status(status).build();
    }

    @Test
    void createBookingSuccessfully() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        BookingDto dto = BookingDto.builder().itemId(10L).start(start).end(start.plusDays(1)).build();
        when(userRepository.findById(2L)).thenReturn(Optional.of(user(2L)));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item()));
        when(bookingRepository.save(any())).thenAnswer(invocation -> {
            Booking saved = invocation.getArgument(0);
            saved.setId(5L);
            return saved;
        });

        BookingDto result = service.create(2L, dto);

        assertEquals(5L, result.getId());
        assertEquals(BookingStatus.WAITING, result.getStatus());
    }

    @Test
    void createBookingRejectsInvalidCases() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.create(2L, BookingDto.builder().build()));

        when(userRepository.findById(2L)).thenReturn(Optional.of(user(2L)));
        assertThrows(ValidationException.class, () -> service.create(2L, BookingDto.builder().build()));

        BookingDto dto = BookingDto.builder().itemId(10L).build();
        when(itemRepository.findById(10L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.create(2L, dto));

        when(itemRepository.findById(10L)).thenReturn(Optional.of(item()));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        assertThrows(ValidationException.class, () -> service.create(1L, BookingDto.builder().itemId(10L).start(LocalDateTime.now().plusDays(1)).end(LocalDateTime.now().plusDays(2)).build()));

        Item unavailable = item();
        unavailable.setAvailable(false);
        when(itemRepository.findById(10L)).thenReturn(Optional.of(unavailable));
        assertThrows(ValidationException.class, () -> service.create(2L, BookingDto.builder().itemId(10L).start(LocalDateTime.now().plusDays(1)).end(LocalDateTime.now().plusDays(2)).build()));

        when(itemRepository.findById(10L)).thenReturn(Optional.of(item()));
        assertThrows(ValidationException.class, () -> service.create(2L, BookingDto.builder().itemId(10L).start(LocalDateTime.now().plusDays(2)).end(LocalDateTime.now().plusDays(1)).build()));
    }

    @Test
    void approveBookingAndRejectInvalidCases() {
        Booking waiting = booking(BookingStatus.WAITING, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        when(bookingRepository.findById(5L)).thenReturn(Optional.of(waiting));
        when(bookingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        assertEquals(BookingStatus.APPROVED, service.approve(1L, 5L, true).getStatus());

        waiting.setStatus(BookingStatus.WAITING);
        assertEquals(BookingStatus.REJECTED, service.approve(1L, 5L, false).getStatus());

        assertThrows(ValidationException.class, () -> service.approve(2L, 5L, true));
        waiting.setStatus(BookingStatus.APPROVED);
        assertThrows(ConflictException.class, () -> service.approve(1L, 5L, true));
        when(bookingRepository.findById(404L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.approve(1L, 404L, true));
    }

    @Test
    void getBookingChecksAccess() {
        Booking booking = booking(BookingStatus.WAITING, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        when(bookingRepository.findById(5L)).thenReturn(Optional.of(booking));

        assertEquals(5L, service.getById(2L, 5L).getId());
        assertEquals(5L, service.getById(1L, 5L).getId());
        assertThrows(NotFoundException.class, () -> service.getById(3L, 5L));
        when(bookingRepository.findById(404L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.getById(2L, 404L));
    }

    @Test
    void getBookingsByBookerAndOwnerChecksUser() {
        when(userRepository.existsById(404L)).thenReturn(false);
        assertThrows(NotFoundException.class, () -> service.getByBooker(404L, BookingState.ALL));
        assertThrows(NotFoundException.class, () -> service.getByOwner(404L, BookingState.ALL));

        when(userRepository.existsById(1L)).thenReturn(true);
        when(bookingRepository.findByBookerIdOrderByStartDesc(1L)).thenReturn(List.of());
        when(bookingRepository.findByItemOwnerIdOrderByStartDesc(1L)).thenReturn(List.of());
        assertTrue(service.getByBooker(1L, BookingState.ALL).isEmpty());
        assertTrue(service.getByOwner(1L, BookingState.ALL).isEmpty());
    }

    @Test
    void filtersBookingsByEveryState() {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = List.of(booking(BookingStatus.WAITING, now.minusDays(1), now.plusDays(1)), booking(BookingStatus.APPROVED, now.minusDays(3), now.minusDays(2)), booking(BookingStatus.APPROVED, now.plusDays(2), now.plusDays(3)), booking(BookingStatus.REJECTED, now.plusDays(4), now.plusDays(5)));
        when(userRepository.existsById(2L)).thenReturn(true);
        when(bookingRepository.findByBookerIdOrderByStartDesc(2L)).thenReturn(bookings);

        assertEquals(4, service.getByBooker(2L, BookingState.ALL).size());
        assertEquals(1, service.getByBooker(2L, BookingState.CURRENT).size());
        assertEquals(1, service.getByBooker(2L, BookingState.PAST).size());
        assertEquals(2, service.getByBooker(2L, BookingState.FUTURE).size());
        assertEquals(1, service.getByBooker(2L, BookingState.WAITING).size());
        assertEquals(1, service.getByBooker(2L, BookingState.REJECTED).size());
    }
}
