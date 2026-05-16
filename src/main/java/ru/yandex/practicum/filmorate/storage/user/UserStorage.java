package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {
    public User addUser(User newUser);

    public User updateUser(User newUser);

    public void deleteUser(User user);

    public Collection<User> getAllUsers();

    public User getUserById(Long id);
}
