package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private static final String SELECT_FILM_FIELDS =
            "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, "
                    + "f.mpa_id, m.name AS mpa_name "
                    + "FROM films f LEFT JOIN mpa m ON f.mpa_id = m.mpa_id";

    private static final String INSERT_FILM =
            "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_FILM =
            "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE film_id = ?";
    private static final String DELETE_FILM = "DELETE FROM films WHERE film_id = ?";

    private static final String SELECT_ALL_FILMS = SELECT_FILM_FIELDS;
    private static final String SELECT_FILM_BY_ID = SELECT_FILM_FIELDS + " WHERE f.film_id = ?";

    private static final String SELECT_GENRES =
            "SELECT g.* FROM genre g JOIN film_genre fg ON g.genre_id = fg.genre_id "
                    + "WHERE fg.film_id = ? ORDER BY g.genre_id";
    private static final String SELECT_LIKES = "SELECT user_id FROM likes WHERE film_id = ?";
    private static final String INSERT_LIKE = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_LIKE = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
    private static final String COUNT_LIKES = "SELECT COUNT(*) FROM likes WHERE film_id = ?";
    private static final String COUNT_LIKE =
            "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?";
    private static final String DELETE_FILM_GENRES = "DELETE FROM film_genre WHERE film_id = ?";
    private static final String INSERT_FILM_GENRE = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";

    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper;

    private final GenreRowMapper genreRowMapper;

    @Override
    public Film addFilm(Film newFilm) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        Long mpaId = extractMpaId(newFilm);
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_FILM, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, newFilm.getName());
            ps.setString(2, newFilm.getDescription());
            ps.setDate(3, Date.valueOf(newFilm.getReleaseDate()));
            ps.setInt(4, newFilm.getDuration());
            if (mpaId != null) {
                ps.setLong(5, mpaId);
            } else {
                ps.setNull(5, java.sql.Types.BIGINT);
            }
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new ValidationException("Не удалось сохранить фильм");
        }
        newFilm.setId(key.longValue());
        saveGenres(newFilm);

        return getFilmById(newFilm.getId());
    }

    @Override
    public Film updateFilm(Film newFilm) {
        int updated = jdbcTemplate.update(UPDATE_FILM,
                newFilm.getName(),
                newFilm.getDescription(),
                Date.valueOf(newFilm.getReleaseDate()),
                newFilm.getDuration(),
                extractMpaId(newFilm),
                newFilm.getId());
        if (updated == 0) {
            throw new ResourceNotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
        }
        saveGenres(newFilm);
        return getFilmById(newFilm.getId());
    }

    @Override
    public void deleteFilm(Film film) {
        if (film.getId() == null) {
            return;
        }
        jdbcTemplate.update(DELETE_FILM, film.getId());
    }

    @Override
    public Collection<Film> getAllFilms() {
        return jdbcTemplate.query(SELECT_ALL_FILMS, filmRowMapper).stream()
                .map(this::enrichFilm)
                .toList();
    }

    @Override
    public Film getFilmById(Long id) {
        try {
            Film film = jdbcTemplate.queryForObject(SELECT_FILM_BY_ID, filmRowMapper, id);
            return enrichFilm(film);
        } catch (EmptyResultDataAccessException e) {
            throw new ResourceNotFoundException("Фильм с id " + id + " не найден");
        }
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        getFilmById(filmId);
        Integer count = jdbcTemplate.queryForObject(COUNT_LIKE, Integer.class, filmId, userId);
        if (count == null || count == 0) {
            jdbcTemplate.update(INSERT_LIKE, filmId, userId);
        }
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        jdbcTemplate.update(DELETE_LIKE, filmId, userId);
    }

    @Override
    public int getLikesCount(Long filmId) {
        Integer count = jdbcTemplate.queryForObject(COUNT_LIKES, Integer.class, filmId);
        return count == null ? 0 : count;
    }

    private Film enrichFilm(Film film) {
        List<Genre> genres = jdbcTemplate.query(SELECT_GENRES, genreRowMapper, film.getId());
        film.setGenres(new LinkedHashSet<>(genres));
        List<Long> likes = jdbcTemplate.queryForList(SELECT_LIKES, Long.class, film.getId());
        film.setLikes(new LinkedHashSet<>(likes));
        return film;
    }

    private void saveGenres(Film film) {
        jdbcTemplate.update(DELETE_FILM_GENRES, film.getId());
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }
        List<Genre> genres = film.getGenres().stream()
                .filter(g -> g.getId() != null)
                .toList();
        if (genres.isEmpty()) {
            return;
        }
        jdbcTemplate.batchUpdate(INSERT_FILM_GENRE, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, film.getId());
                ps.setLong(2, genres.get(i).getId());
            }

            @Override
            public int getBatchSize() {
                return genres.size();
            }
        });
    }

    private Long extractMpaId(Film film) {
        return film.getMpa() != null ? film.getMpa().getId() : null;
    }
}
