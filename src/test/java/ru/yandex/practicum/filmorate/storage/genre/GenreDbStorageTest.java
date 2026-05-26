package ru.yandex.practicum.filmorate.storage.genre;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({GenreDbStorage.class, GenreRowMapper.class})
class GenreDbStorageTest {

    @Autowired
    private GenreDbStorage genreStorage;

    @Test
    void testFindAll() {
        Collection<Genre> genres = genreStorage.findAll();

        assertThat(genres).hasSize(6);
    }

    @Test
    void testFindById() {
        Optional<Genre> genre = genreStorage.findById(1);

        assertThat(genre).isPresent();
        assertThat(genre.get().getName()).isEqualTo("Комедия");
    }

    @Test
    void testFindByIdNotFound() {
        Optional<Genre> genre = genreStorage.findById(999);

        assertThat(genre).isEmpty();
    }
}
