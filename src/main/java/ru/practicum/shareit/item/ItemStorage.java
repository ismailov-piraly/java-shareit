package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ItemStorage {

    private final Map<Long, Item> items = new HashMap<>();

    private long nextId = 1;

    public Item save(Item item) {

        item.setId(nextId++);

        items.put(item.getId(), item);

        return item;
    }

    public Item findById(Long itemId) {
        return items.get(itemId);
    }

    public List<Item> findByOwnerId(Long userId) {

        return items.values().stream().filter(item -> item.getOwner().getId().equals(userId)).toList();
    }

    public List<Item> search(String text) {

        String searchText = text.toLowerCase();

        return items.values().stream().filter(Item::getAvailable).filter(item -> item.getName().toLowerCase().contains(searchText) || item.getDescription().toLowerCase().contains(searchText)).toList();
    }
}