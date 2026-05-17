package ru.yandex.practicum.filmorate.storage.film;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.genre.GenreRowMapper;
import ru.yandex.practicum.filmorate.storage.mpa.MpaRowMapper;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserRowMapper;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, FilmRowMapper.class, MpaRowMapper.class, GenreRowMapper.class,
        UserDbStorage.class, UserRowMapper.class})
class FilmDbStorageTest {

    @Autowired
    private FilmDbStorage filmStorage;

    @Autowired
    private UserDbStorage userStorage;

    private Film film;
    private User user;

    @BeforeEach
    void setUp() {
        Mpa mpa = new Mpa();
        mpa.setId(1L);

        Genre genre1 = new Genre();
        genre1.setId(1L);
        Genre genre2 = new Genre();
        genre2.setId(2L);

        film = new Film();
        film.setName("Test Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(mpa);
        film.setGenres(Set.of(genre1, genre2));
        film = filmStorage.addFilm(film);

        user = new User();
        user.setEmail("liker@test.com");
        user.setLogin("likerlogin");
        user.setName("Liker");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        user = userStorage.addUser(user);
    }

    @Test
    void testAddAndGetFilmById() {
        Film found = filmStorage.getFilmById(film.getId());

        assertThat(found.getName()).isEqualTo("Test Film");
        assertThat(found.getMpa()).isNotNull();
        assertThat(found.getMpa().getId()).isEqualTo(1L);
        assertThat(found.getMpa().getName()).isEqualTo("G");
        assertThat(found.getGenres()).hasSize(2);
        assertThat(found.getLikes()).isEmpty();
    }

    @Test
    void testUpdateFilm() {
        film.setName("Updated Film");
        Genre genre3 = new Genre();
        genre3.setId(3L);
        film.setGenres(Set.of(genre3));

        Film updated = filmStorage.updateFilm(film);

        assertThat(updated.getName()).isEqualTo("Updated Film");
        assertThat(updated.getGenres()).extracting(Genre::getId).containsExactly(3L);
    }

    @Test
    void testGetAllFilms() {
        Collection<Film> films = filmStorage.getAllFilms();

        assertThat(films).hasSize(1);
    }

    @Test
    void testDeleteFilm() {
        filmStorage.deleteFilm(film);

        assertThatThrownBy(() -> filmStorage.getFilmById(film.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void testGetFilmByIdNotFound() {
        assertThatThrownBy(() -> filmStorage.getFilmById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void testAddAndRemoveLike() {
        filmStorage.addLike(film.getId(), user.getId());
        filmStorage.addLike(film.getId(), user.getId());

        assertThat(filmStorage.getLikesCount(film.getId())).isEqualTo(1);

        filmStorage.removeLike(film.getId(), user.getId());

        assertThat(filmStorage.getLikesCount(film.getId())).isZero();
    }
}
