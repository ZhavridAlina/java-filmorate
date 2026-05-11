package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public User addUser(User newUser) {
        if (newUser.getName() == null || newUser.getName().isBlank()) {
            newUser.setName(newUser.getLogin());
        }
        return userStorage.addUser(newUser);
    }

    public User updateUser(User newUser) {
        if (newUser.getName() == null || newUser.getName().isBlank()) {
            newUser.setName(newUser.getLogin());
        }
        return userStorage.updateUser(newUser);
    }

    public void deleteUser(User user) {
        userStorage.deleteUser(user);
    }

    public Collection<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public User getUserById(Long id) {
        return userStorage.getUserById(id);
    }

    public void addFriend(Long userId, Long friendId) {
        if (Objects.equals(userId, friendId)) {
            return;
        }
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);
        user.getFriends().add(friend.getId());
        friend.getFriends().add(user.getId());
    }

    public void deleteFriend(Long userId, Long friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);
        user.getFriends().remove(friend.getId());
        friend.getFriends().remove(user.getId());
    }

    public Collection<User> getFriends(Long userId) {
        User user = userStorage.getUserById(userId);
        return user.getFriends().stream()
                .map(userStorage::getUserById)
                .collect(Collectors.toList());
    }

    public Collection<User> getCommonFriends(Long userId, Long otherId) {
        User user = userStorage.getUserById(userId);
        User other = userStorage.getUserById(otherId);
        if (user.getFriends().isEmpty() || other.getFriends().isEmpty()) {
            return Collections.emptyList();
        }
        List<User> common = user.getFriends().stream()
                .filter(id -> other.getFriends().contains(id))
                .map(userStorage::getUserById)
                .collect(Collectors.toList());
        return common;
    }
}
