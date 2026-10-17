package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.BookingShortDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    public ItemDto addItem(Long userId, ItemDto itemDto) {
        User owner = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        validateItem(itemDto);
        Item item = ItemMapper.toModel(itemDto);
        item.setOwner(owner);
        log.info("Добавление вещи пользователем с id={}", userId);
        return ItemMapper.toDto(itemRepository.save(item));
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException("Вещь с id " + itemId + " не найдена"));
        if (!item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Пользователь не является владельцем вещи");
        }
        if (itemDto.getName() != null) {
            if (itemDto.getName().isBlank()) throw new ValidationException("Название вещи не может быть пустым");
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            if (itemDto.getDescription().isBlank()) throw new ValidationException("Описание вещи не может быть пустым");
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) item.setAvailable(itemDto.getAvailable());
        log.info("Обновление вещи id={} пользователем id={}", itemId, userId);
        return ItemMapper.toDto(itemRepository.save(item));
    }

    @Override
    public ItemDto getItemById(Long itemId) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException("Вещь с id " + itemId + " не найдена"));
        ItemDto dto = ItemMapper.toDto(item);
        dto.setComments(commentRepository.findByItemIdOrderByCreatedDesc(itemId).stream().map(CommentMapper::toDto).toList());
        return dto;
    }

    @Override
    public List<ItemDto> getItemsByOwner(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        return itemRepository.findByOwnerIdOrderById(userId).stream().map(item -> {
            ItemDto dto = ItemMapper.toDto(item);
            dto.setLastBooking(bookingRepository.findFirstByItemIdAndStatusAndEndLessThanEqualOrderByEndDesc(item.getId(), BookingStatus.APPROVED, LocalDateTime.now()).map(b -> new BookingShortDto(b.getId(), b.getBooker().getId())).orElse(null));
            dto.setNextBooking(bookingRepository.findFirstByItemIdAndStatusAndStartGreaterThanEqualOrderByStartAsc(item.getId(), BookingStatus.APPROVED, LocalDateTime.now()).map(b -> new BookingShortDto(b.getId(), b.getBooker().getId())).orElse(null));
            dto.setComments(commentRepository.findByItemIdOrderByCreatedDesc(item.getId()).stream().map(CommentMapper::toDto).toList());
            return dto;
        }).toList();
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.isBlank()) return List.of();
        return itemRepository.searchAvailable(text.trim()).stream().map(ItemMapper::toDto).toList();
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, String text) {
        User author = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException("Вещь с id " + itemId + " не найдена"));
        if (text == null || text.isBlank()) throw new ValidationException("Текст комментария не может быть пустым");
        if (!bookingRepository.existsByItemIdAndBookerIdAndStatusAndEndLessThanEqual(itemId, userId, BookingStatus.APPROVED, LocalDateTime.now())) {
            throw new ValidationException("Оставить отзыв можно только после завершённого бронирования");
        }
        Comment comment = Comment.builder().text(text).item(item).author(author).created(LocalDateTime.now()).build();
        return CommentMapper.toDto(commentRepository.save(comment));
    }

    private void validateItem(ItemDto dto) {
        if (dto.getName() == null || dto.getName().isBlank())
            throw new ValidationException("Название вещи не может быть пустым");
        if (dto.getDescription() == null || dto.getDescription().isBlank())
            throw new ValidationException("Описание вещи не может быть пустым");
        if (dto.getAvailable() == null) throw new ValidationException("Поле available не может быть null");
    }
}
