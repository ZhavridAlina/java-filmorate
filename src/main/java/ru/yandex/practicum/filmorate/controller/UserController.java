package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    @PostMapping
    public User addUser(@Valid @RequestBody User user) {
        log.info("Получен запрос на добавление пользователя с логином:{}", user.getLogin());
        user.setId(getNextId());
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        users.put(user.getId(), user);
        log.info("Пользователь с логином {} успешно добавлен", user.getLogin());
        return user;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User newUser) {
        log.info("Получен запрос на изменение пользователя с логином:{}", newUser.getLogin());
        if (newUser.getId() == null) {
            log.error("Ошибка валидации при изменении пользователя с логином {},Id не указан", newUser.getLogin());
            throw new ValidationException("Id не может быть пустым");
        }
        if (users.containsKey(newUser.getId())) {
            if (newUser.getName() == null || newUser.getName().isBlank()) {
                newUser.setName(newUser.getLogin());
            }
            users.put(newUser.getId(), newUser);
            log.info("Пользователь с логином {} успешно изменен", newUser.getLogin());
            return newUser;
        }
        log.error("Ошибка валидации при изменении пользователя с логином {},ID {} не найден", newUser.getLogin(), newUser.getId());
        throw new ValidationException("Пользователь с id = " + newUser.getId() + " не найден");
    }

    @GetMapping
    public Collection<User> getAllUsers() {
        log.info("Получен запрос на получение списка всех пользователей");
        return users.values();
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
