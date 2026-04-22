package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPassWhenAllFieldsValid() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("validLogin");
        user.setName("Valid Name");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailWhenEmailIsBlankOrInvalid() {
        User user = new User();
        user.setLogin("login");
        user.setBirthday(LocalDate.now());

        // пустой email
        user.setEmail("");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("email");

        // email без @
        user.setEmail("invalid-email");
        violations = validator.validate(user);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("email");
    }

    @Test
    void shouldFailWhenLoginIsBlank() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setLogin("");
        user.setBirthday(LocalDate.now());

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertThat(violations).hasSize(2); // обе аннотации сработали
    }

    @Test
    void shouldFailWhenLoginContainsSpaces() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setLogin("user name");
        user.setBirthday(LocalDate.now());

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertThat(violations).hasSize(1); // только @Pattern
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Логин не должен содержать пробелы");
    }

    @Test
    void shouldPassWhenNameIsNull() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setLogin("login");
        user.setName(null);
        user.setBirthday(LocalDate.now());

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertThat(violations).isEmpty(); // аннотаций на name нет
    }

    @Test
    void shouldFailWhenBirthdayIsInFuture() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setLogin("login");
        user.setBirthday(LocalDate.now().plusDays(1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("birthday");
    }

    @Test
    void shouldPassWhenBirthdayIsPastOrPresent() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setLogin("login");
        user.setBirthday(LocalDate.now());

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertThat(violations).isEmpty();
    }
}
