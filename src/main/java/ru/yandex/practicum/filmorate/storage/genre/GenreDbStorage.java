package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {

    private static final String SELECT_ALL = "SELECT * FROM genre ORDER BY genre_id";
    private static final String SELECT_BY_ID = "SELECT * FROM genre WHERE genre_id = ?";

    private final JdbcTemplate jdbcTemplate;
    private final GenreRowMapper genreRowMapper;

    @Override
    public Collection<Genre> findAll() {
        return jdbcTemplate.query(SELECT_ALL, genreRowMapper).stream()
                .sorted(Comparator.comparing(Genre::getId))
                .toList();
    }

    @Override
    public Optional<Genre> findById(long id) {
        try {
            Genre genre = jdbcTemplate.queryForObject(SELECT_BY_ID, genreRowMapper, id);
            return Optional.ofNullable(genre);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
