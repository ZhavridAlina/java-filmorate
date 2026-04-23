package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        log.info("Получен запрос на добавление фильма:{}", film.getName());
        if (!film.isReleaseDateValid()) {
            log.error("Ошибка валидации при добавлении фильма {}", film.getName());
            throw new ValidationException("Ошибка при валидации фильма");
        }
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Фильм {} успешно добавлен", film.getName());

        return film;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film newFilm) {
        log.info("Получен запрос на изменение фильма {}", newFilm.getName());
        if (newFilm.getId() == null) {
            log.error("Ошибка валидации при изменении фильма {},Id не указан", newFilm.getName());
            throw new ValidationException("Id не может быть пустым");
        }
        if (films.containsKey(newFilm.getId())) {
            if (!newFilm.isReleaseDateValid()) {
                log.error("Ошибка валидации при изменении фильма {}", newFilm.getId());
                throw new ValidationException("Ошибка валидации фильма");
            }
            films.put(newFilm.getId(), newFilm);
            log.info("Фильм {} успешно изменен", newFilm.getName());
            return newFilm;
        }
        log.error("Ошибка валидации при изменении фильма {},ID {} не найден", newFilm.getName(), newFilm.getId());
        throw new ValidationException("Фильм с id = " + newFilm.getId() + " не найден");
    }

    @GetMapping
    public Collection<Film> getAllFilms() {
        log.info("Получен запрос на получение списка всех фильмов");
        return films.values();
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
