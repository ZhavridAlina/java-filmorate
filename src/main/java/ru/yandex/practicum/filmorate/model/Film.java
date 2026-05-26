package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Data
public class Film {
    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    private Long id;

    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @Size(max = 200, message = "Максимальная длина описания 200 символов")
    private String description;

    private LocalDate releaseDate;

    @Positive(message = "Продолжительность должна быть положительным числом")
    private Integer duration;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    private Mpa mpa;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    private Set<Genre> genres = new LinkedHashSet<>();

    private Set<Long> likes = new LinkedHashSet<>();

    @AssertTrue(message = "дата релиза не может быть раньше 28 декабря 1895 года")
    public boolean isReleaseDateValid() {
        return releaseDate != null && !releaseDate.isBefore(CINEMA_BIRTHDAY);
    }
}
