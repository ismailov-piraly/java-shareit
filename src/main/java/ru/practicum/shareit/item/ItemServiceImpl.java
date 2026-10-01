package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    @Override
    public ItemDto addItem(Long userId, ItemDto itemDto) {

        log.info("Добавление вещи пользователем с id={}", userId);

        User owner = userStorage.findById(userId);

        if (owner == null) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        validateItem(itemDto);

        Item item = ItemMapper.toModel(itemDto);

        item.setOwner(owner);

        return ItemMapper.toDto(itemStorage.save(item));
    }

    @Override
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {

        log.info("Обновление вещи с id={} пользователем с id={}", itemId, userId);

        Item item = itemStorage.findById(itemId);

        if (item == null) {
            throw new NotFoundException("Вещь с id " + itemId + " не найдена");
        }

        if (!item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Пользователь не является владельцем вещи");
        }

        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }

        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }

        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        return ItemMapper.toDto(item);
    }

    @Override
    public ItemDto getItemById(Long itemId) {
        log.info("Получение вещи с id={}", itemId);

        Item item = itemStorage.findById(itemId);

        if (item == null) {
            throw new NotFoundException("Вещь с id " + itemId + " не найдена");
        }

        return ItemMapper.toDto(item);
    }

    @Override
    public List<ItemDto> getItemsByOwner(Long userId) {

        log.info("Получение вещей пользователя с id={}", userId);

        if (!userStorage.existsById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        return itemStorage.findByOwnerId(userId).stream().map(ItemMapper::toDto).toList();
    }

    @Override
    public List<ItemDto> searchItems(String text) {

        log.info("Поиск вещей по тексту: {}", text);

        if (text == null || text.isBlank()) {
            return List.of();
        }

        return itemStorage.search(text).stream().map(ItemMapper::toDto).toList();
    }

    private void validateItem(ItemDto itemDto) {

        if (itemDto.getName() == null || itemDto.getName().isBlank()) {

            throw new ValidationException("Название вещи не может быть пустым");
        }

        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {

            throw new ValidationException("Описание вещи не может быть пустым");
        }

        if (itemDto.getAvailable() == null) {

            throw new ValidationException("Поле available не может быть null");
        }
    }
}