package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface UserStorage {
    User addUser(User newUser);

    User updateUser(User newUser);

    void deleteUser(User user);

    Collection<User> getAllUsers();

    User getUserById(Long id);

    Optional<User> findUserById(long id);

    void addFriend(Long userId, Long friendId);

    void removeFriend(Long userId, Long friendId);

    Set<Long> getFriendIds(Long userId);
}
