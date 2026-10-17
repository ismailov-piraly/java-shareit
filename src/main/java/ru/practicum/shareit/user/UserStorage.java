package ru.practicum.shareit.user;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class UserStorage {

    private final Map<Long, User> users = new HashMap<>();

    private long nextId = 1;

    public User save(User user) {
        user.setId(nextId++);
        users.put(user.getId(), user);

        return user;
    }

    public User findById(Long userId) {
        return users.get(userId);
    }

    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    public void update(User user) {
        users.put(user.getId(), user);
    }

    public boolean existsById(Long userId) {
        return users.containsKey(userId);
    }

    public boolean existsByEmail(String email) {
        return users.values().stream().anyMatch(user -> user.getEmail().equalsIgnoreCase(email));
    }

    public boolean existsByEmailAndNotId(String email, Long userId) {
        return users.values().stream().anyMatch(user -> !user.getId().equals(userId) && user.getEmail().equalsIgnoreCase(email));
    }

    public void delete(Long userId) {
        users.remove(userId);
    }
}