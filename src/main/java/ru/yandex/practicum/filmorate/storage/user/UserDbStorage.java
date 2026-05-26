package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private static final String INSERT_USER =
            "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_USER =
            "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";
    private static final String DELETE_USER = "DELETE FROM users WHERE user_id = ?";
    private static final String SELECT_ALL_USERS = "SELECT * FROM users";
    private static final String SELECT_USER_BY_ID = "SELECT * FROM users WHERE user_id = ?";
    private static final String SELECT_FRIENDS = "SELECT friend_id FROM friends WHERE user_id = ?";
    private static final String INSERT_FRIEND =
            "INSERT INTO friends (user_id, friend_id) VALUES (?, ?)";
    private static final String DELETE_FRIEND = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
    private static final String COUNT_FRIENDSHIP =
            "SELECT COUNT(*) FROM friends WHERE user_id = ? AND friend_id = ?";

    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;

    @Override
    public User addUser(User newUser) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_USER, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, newUser.getEmail());
            ps.setString(2, newUser.getLogin());
            ps.setString(3, newUser.getName());
            ps.setDate(4, Date.valueOf(newUser.getBirthday()));
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new ValidationException("Не удалось сохранить пользователя");
        }
        newUser.setId(key.longValue());
        newUser.setFriends(new HashSet<>());
        return newUser;
    }

    @Override
    public User updateUser(User newUser) {
        if (newUser.getId() == null) {
            throw new ValidationException("Id не может быть пустым");
        }
        getUserById(newUser.getId());
        int updated = jdbcTemplate.update(UPDATE_USER,
                newUser.getEmail(),
                newUser.getLogin(),
                newUser.getName(),
                Date.valueOf(newUser.getBirthday()),
                newUser.getId());
        if (updated == 0) {
            throw new ResourceNotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
        }
        newUser.setFriends(getFriendIds(newUser.getId()));
        return newUser;
    }

    @Override
    public void deleteUser(User user) {
        if (user.getId() == null) {
            return;
        }
        jdbcTemplate.update(DELETE_USER, user.getId());
    }

    @Override
    public Collection<User> getAllUsers() {
        return jdbcTemplate.query(SELECT_ALL_USERS, userRowMapper).stream()
                .map(this::enrichWithFriends)
                .toList();
    }

    @Override
    public User getUserById(Long id) {
        return enrichWithFriends(findUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь с id " + id + " не найден")));
    }

    @Override
    public Optional<User> findUserById(long id) {
        try {
            User user = jdbcTemplate.queryForObject(SELECT_USER_BY_ID, userRowMapper, id);
            return Optional.of(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        if (Objects.equals(userId, friendId)) {
            return;
        }
        getUserById(userId);
        getUserById(friendId);

        Integer exists = jdbcTemplate.queryForObject(COUNT_FRIENDSHIP, Integer.class, userId, friendId);
        if (exists == null || exists == 0) {
            jdbcTemplate.update(INSERT_FRIEND, userId, friendId);
        }
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        getUserById(userId);
        getUserById(friendId);
        jdbcTemplate.update(DELETE_FRIEND, userId, friendId);
    }

    @Override
    public Set<Long> getFriendIds(Long userId) {
        return new HashSet<>(jdbcTemplate.queryForList(SELECT_FRIENDS, Long.class, userId));
    }

    private User enrichWithFriends(User user) {
        user.setFriends(getFriendIds(user.getId()));
        return user;
    }
}
