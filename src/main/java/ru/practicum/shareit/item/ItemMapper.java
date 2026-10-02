package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

public class ItemMapper {

    private ItemMapper() {
    }

    public static ItemDto toDto(Item item) {
        return ItemDto.builder().id(item.getId()).name(item.getName()).description(item.getDescription()).available(item.getAvailable()).build();
    }

    public static Item toModel(ItemDto dto) {
        return Item.builder().id(dto.getId()).name(dto.getName()).description(dto.getDescription()).available(dto.getAvailable()).build();
    }
}