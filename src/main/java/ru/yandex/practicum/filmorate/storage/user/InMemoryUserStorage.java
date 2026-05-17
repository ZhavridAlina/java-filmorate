package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.StorageQualifier;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Component
@Qualifier(StorageQualifier.USER_MEMORY)
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public User addUser(User newUser) {
        if (newUser.getFriends() == null) {
            newUser.setFriends(new HashSet<>());
        }
        newUser.setId(getNextId());
        users.put(newUser.getId(), newUser);
        return newUser;
    }

    @Override
    public User updateUser(User newUser) {
        if (newUser.getId() == null) {
            throw new ValidationException("Id не может быть пустым");
        }
        if (users.containsKey(newUser.getId())) {
            User existing = users.get(newUser.getId());
            if (newUser.getFriends() == null) {
                newUser.setFriends(existing.getFriends());
            }
            users.put(existing.getId(), newUser);
            return newUser;
        }
        throw new ResourceNotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
    }

    @Override
    public void deleteUser(User user) {
        if (user.getId() == null) {
            return;
        }
        users.remove(user.getId());
    }

    @Override
    public Collection<User> getAllUsers() {
        return users.values();
    }

    @Override
    public User getUserById(Long id) {
        User user = users.get(id);
        if (user == null) {
            throw new ResourceNotFoundException("Пользователь с id " + id + " не найден");
        }
        return user;
    }

    @Override
    public Optional<User> findUserById(long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        if (Objects.equals(userId, friendId)) {
            return;
        }
        User user = getUserById(userId);
        getUserById(friendId);
        user.getFriends().add(friendId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        User user = getUserById(userId);
        getUserById(friendId);
        user.getFriends().remove(friendId);
    }

    @Override
    public Set<Long> getFriendIds(Long userId) {
        return new HashSet<>(getUserById(userId).getFriends());
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
