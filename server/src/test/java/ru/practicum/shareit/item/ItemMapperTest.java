package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ItemMapperTest {
    @Test
    void mapsItemToDtoWithAndWithoutRequest() {
        Item item = Item.builder().id(1L).name("Дрель").description("Описание").available(true).build();

        ItemDto withoutRequest = ItemMapper.toDto(item);
        assertEquals(1L, withoutRequest.getId());
        assertNull(withoutRequest.getRequestId());

        item.setRequest(ItemRequest.builder().id(9L).description("Запрос").created(LocalDateTime.now()).build());
        assertEquals(9L, ItemMapper.toDto(item).getRequestId());
    }

    @Test
    void mapsDtoToItem() {
        ItemDto dto = ItemDto.builder().id(2L).name("Шуруповёрт").description("Описание").available(false).build();

        Item item = ItemMapper.toModel(dto);

        assertEquals(dto.getId(), item.getId());
        assertEquals(dto.getName(), item.getName());
        assertEquals(dto.getDescription(), item.getDescription());
        assertFalse(item.getAvailable());
    }
}
