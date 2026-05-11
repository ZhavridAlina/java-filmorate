package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Film addFilm(Film newFilm) {
        if (newFilm.getLikes() == null) {
            newFilm.setLikes(new HashSet<>());
        }
        newFilm.setId(getNextId());
        films.put(newFilm.getId(), newFilm);

        return newFilm;
    }

    @Override
    public Film updateFilm(Film newFilm) {
        if (!films.containsKey(newFilm.getId())) {
            throw new ResourceNotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
        }

        films.put(newFilm.getId(), newFilm);
        return newFilm;

    }

    @Override
    public void deleteFilm(Film film) {
        if (films.containsKey(film.getId())) {
            films.remove(film.getId());
        }
    }

    @Override
    public Collection<Film> getAllFilms() {
        return films.values();
    }

    @Override
    public Film getFilmById(Long id) {
        Film film = films.get(id);
        if (film == null) {
            throw new ResourceNotFoundException("Фильм с id " + id + " не найден");
        }
        return film;
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
