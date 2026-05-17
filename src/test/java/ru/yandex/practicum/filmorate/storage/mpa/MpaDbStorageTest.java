package ru.yandex.practicum.filmorate.storage.mpa;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({MpaDbStorage.class, MpaRowMapper.class})
class MpaDbStorageTest {

    @Autowired
    private MpaDbStorage mpaStorage;

    @Test
    void testFindAll() {
        Collection<Mpa> mpas = mpaStorage.findAll();

        assertThat(mpas).hasSize(5);
        assertThat(mpas).extracting(Mpa::getName)
                .containsExactly("G", "PG", "PG-13", "R", "NC-17");
    }

    @Test
    void testFindById() {
        Optional<Mpa> mpa = mpaStorage.findById(1);

        assertThat(mpa).isPresent();
        assertThat(mpa.get()).hasFieldOrPropertyWithValue("id", 1L);
        assertThat(mpa.get().getName()).isEqualTo("G");
    }

    @Test
    void testFindByIdNotFound() {
        Optional<Mpa> mpa = mpaStorage.findById(999);

        assertThat(mpa).isEmpty();
    }
}
