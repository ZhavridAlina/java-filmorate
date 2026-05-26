package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {

    private static final String SELECT_ALL = "SELECT * FROM mpa ORDER BY mpa_id";
    private static final String SELECT_BY_ID = "SELECT * FROM mpa WHERE mpa_id = ?";

    private final JdbcTemplate jdbcTemplate;
    private final MpaRowMapper mpaRowMapper;

    @Override
    public Collection<Mpa> findAll() {
        return jdbcTemplate.query(SELECT_ALL, mpaRowMapper).stream()
                .sorted(Comparator.comparing(Mpa::getId))
                .toList();
    }

    @Override
    public Optional<Mpa> findById(long id) {
        try {
            Mpa mpa = jdbcTemplate.queryForObject(SELECT_BY_ID, mpaRowMapper, id);
            return Optional.ofNullable(mpa);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
