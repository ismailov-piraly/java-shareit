package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public BookingDto create(Long userId, BookingDto dto) {
        User booker = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        if (dto.getItemId() == null) {
            throw new ValidationException("Не указана вещь для бронирования");
        }
        Item item = itemRepository.findById(dto.getItemId()).orElseThrow(() -> new NotFoundException("Вещь не найдена"));
        if (item.getOwner().getId().equals(userId)) {
            throw new ValidationException("Нельзя бронировать собственную вещь");
        }
        if (!Boolean.TRUE.equals(item.getAvailable())) {
            throw new ValidationException("Вещь недоступна для бронирования");
        }
        if (dto.getStart() == null || dto.getEnd() == null || !dto.getStart().isBefore(dto.getEnd()) || !dto.getStart().isAfter(LocalDateTime.now())) {
            throw new ValidationException("Даты бронирования указаны некорректно");
        }
        Booking booking = Booking.builder().start(dto.getStart()).end(dto.getEnd()).item(item).booker(booker).status(BookingStatus.WAITING).build();
        log.info("Создание бронирования вещи id={} пользователем id={}", item.getId(), userId);
        return BookingMapper.toDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingDto approve(Long userId, Long bookingId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new NotFoundException("Бронирование с id " + bookingId + " не найдено"));
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ValidationException("Подтвердить бронирование может только владелец вещи");
        }
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ConflictException("Статус бронирования уже установлен");
        }
        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);

        log.info("Бронирование id={} пользователем id={} {}", bookingId, userId, approved ? "подтверждено" : "отклонено");

        return BookingMapper.toDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto getById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new NotFoundException("Бронирование с id " + bookingId + " не найдено"));
        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("Нет доступа к этому бронированию");
        }
        return BookingMapper.toDto(booking);
    }

    @Override
    public List<BookingDto> getByBooker(Long userId, BookingState state) {
        ensureUserExists(userId);

        log.info("Получение бронирований пользователя id={} со статусом {}", userId, state);

        return filter(bookingRepository.findByBookerIdOrderByStartDesc(userId), state);
    }

    @Override
    public List<BookingDto> getByOwner(Long userId, BookingState state) {
        ensureUserExists(userId);

        log.info("Получение бронирований владельца id={} со статусом {}", userId, state);

        return filter(bookingRepository.findByItemOwnerIdOrderByStartDesc(userId), state);
    }

    private void ensureUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
    }

    private List<BookingDto> filter(List<Booking> bookings, BookingState state) {
        LocalDateTime now = LocalDateTime.now();
        return bookings.stream().filter(b -> switch (state) {
            case ALL -> true;
            case CURRENT -> !b.getStart().isAfter(now) && b.getEnd().isAfter(now);
            case PAST -> !b.getEnd().isAfter(now);
            case FUTURE -> b.getStart().isAfter(now);
            case WAITING -> b.getStatus() == BookingStatus.WAITING;
            case REJECTED -> b.getStatus() == BookingStatus.REJECTED;
        }).sorted(Comparator.comparing(Booking::getStart).reversed()).map(BookingMapper::toDto).toList();
    }
}
