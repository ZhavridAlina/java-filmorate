package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class FilmValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPassWhenAllFieldsValid() {
        Film film = new Film();
        film.setName("Valid Name");
        film.setDescription("Valid description within 200 chars");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailWhenNameIsBlank() {
        Film film = new Film();
        film.setName(""); // пустая строка
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(90);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("name");
    }

    @Test
    void shouldFailWhenDescriptionExceeds200Chars() {
        Film film = new Film();
        film.setName("Name");
        film.setDescription("a".repeat(201));
        film.setReleaseDate(LocalDate.now());
        film.setDuration(90);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("description");
    }

    @Test
    void shouldPassWhenDescriptionIsNull() {
        Film film = new Film();
        film.setName("Name");
        film.setDescription(null);
        film.setReleaseDate(LocalDate.now());
        film.setDuration(90);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailWhenDurationIsZeroOrNegative() {
        Film film = new Film();
        film.setName("Name");
        film.setDescription("Desc");
        film.setReleaseDate(LocalDate.now());

        film.setDuration(0);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("duration");

        film.setDuration(-5);
        violations = validator.validate(film);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("duration");
    }
}
