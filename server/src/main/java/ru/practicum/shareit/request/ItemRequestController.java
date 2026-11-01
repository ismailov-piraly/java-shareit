package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestRepository requests;
    private final ItemRepository items;
    private final UserRepository users;

    private User user(Long id) {
        return users.findById(id).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    private ItemRequestDto map(ItemRequest r) {
        List<ItemRequestDto.ResponseItemDto> answers = items.findByRequestId(r.getId()).stream().map(i -> new ItemRequestDto.ResponseItemDto(i.getId(), i.getName(), i.getOwner().getId())).toList();
        return new ItemRequestDto(r.getId(), r.getDescription(), r.getCreated(), answers);
    }

    @PostMapping
    @Transactional
    public ItemRequestDto create(@RequestHeader("X-Sharer-User-Id") Long userId, @RequestBody ItemRequestDto dto) {
        User requester = user(userId);
        if (dto.getDescription() == null || dto.getDescription().isBlank())
            throw new ValidationException("Описание запроса обязательно");
        return map(requests.save(ItemRequest.builder().description(dto.getDescription()).requester(requester).created(LocalDateTime.now()).build()));
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<ItemRequestDto> mine(@RequestHeader("X-Sharer-User-Id") Long userId) {
        user(userId);
        return requests.findByRequesterIdOrderByCreatedDesc(userId).stream().map(this::map).toList();
    }

    @GetMapping("/all")
    @Transactional(readOnly = true)
    public List<ItemRequestDto> all(@RequestHeader("X-Sharer-User-Id") Long userId, @RequestParam(defaultValue = "0") int from, @RequestParam(defaultValue = "20") int size) {
        user(userId);
        if (from < 0 || size < 1) throw new ValidationException("Некорректная пагинация");
        List<ItemRequestDto> list = requests.findByRequesterIdNotOrderByCreatedDesc(userId).stream().map(this::map).toList();
        return list.stream().skip(from).limit(size).toList();
    }

    @GetMapping("/{requestId}")
    @Transactional(readOnly = true)
    public ItemRequestDto one(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long requestId) {
        user(userId);
        return map(requests.findById(requestId).orElseThrow(() -> new NotFoundException("Запрос не найден")));
    }
}
