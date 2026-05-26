package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;

    public FilmService(FilmStorage filmStorage,
                       UserStorage userStorage,
                       MpaStorage mpaStorage,
                       GenreStorage genreStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
    }

    public Film addFilm(Film film) {
        validateMpaAndGenres(film);
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        validateMpaAndGenres(film);
        return filmStorage.updateFilm(film);
    }

    public Collection<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public Film getFilmById(Long id) {
        return filmStorage.getFilmById(id);
    }

    public void deleteFilm(Film film) {
        filmStorage.deleteFilm(film);
    }

    public void addLike(Long filmId, Long userId) {
        userStorage.getUserById(userId);
        filmStorage.addLike(filmId, userId);
    }

    public void deleteLike(Long filmId, Long userId) {
        userStorage.getUserById(userId);
        filmStorage.removeLike(filmId, userId);
    }

    public Collection<Film> getPopularFilms(int count) {
        return filmStorage.getAllFilms().stream()
                .sorted(Comparator.<Film>comparingInt(f -> filmStorage.getLikesCount(f.getId())).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    private void validateMpaAndGenres(Film film) {
        if (film.getMpa() != null && film.getMpa().getId() != null) {
            mpaStorage.findById(film.getMpa().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Рейтинг MPA с id " + film.getMpa().getId() + " не найден"));
        }
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                if (genre.getId() != null) {
                    genreStorage.findById(genre.getId())
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Жанр с id " + genre.getId() + " не найден"));
                }
            }
        }
    }
}
