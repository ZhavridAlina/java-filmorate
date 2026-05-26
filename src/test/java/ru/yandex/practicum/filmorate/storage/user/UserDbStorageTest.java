package ru.yandex.practicum.filmorate.storage.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserDbStorage.class, UserRowMapper.class})
class UserDbStorageTest {

    @Autowired
    private UserDbStorage userStorage;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("user@test.com");
        user.setLogin("userlogin");
        user.setName("User Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        user = userStorage.addUser(user);
    }

    @Test
    void testFindUserById() {
        Optional<User> userOptional = userStorage.findUserById(user.getId());

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(found ->
                        assertThat(found).hasFieldOrPropertyWithValue("id", user.getId())
                );
    }

    @Test
    void testAddAndGetUserById() {
        User found = userStorage.getUserById(user.getId());

        assertThat(found.getEmail()).isEqualTo(user.getEmail());
        assertThat(found.getFriends()).isEmpty();
    }

    @Test
    void testUpdateUser() {
        user.setName("Updated Name");
        User updated = userStorage.updateUser(user);

        assertThat(updated.getName()).isEqualTo("Updated Name");
        assertThat(userStorage.getUserById(user.getId()).getName()).isEqualTo("Updated Name");
    }

    @Test
    void testGetAllUsers() {
        Collection<User> users = userStorage.getAllUsers();

        assertThat(users).hasSize(1);
    }

    @Test
    void testDeleteUser() {
        userStorage.deleteUser(user);

        assertThat(userStorage.findUserById(user.getId())).isEmpty();
    }

    @Test
    void testGetUserByIdNotFound() {
        assertThatThrownBy(() -> userStorage.getUserById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void testOneSidedFriendship() {
        User friend = createUser("friend@test.com", "friendlogin");

        userStorage.addFriend(user.getId(), friend.getId());

        Set<Long> userFriends = userStorage.getFriendIds(user.getId());
        Set<Long> friendFriends = userStorage.getFriendIds(friend.getId());

        assertThat(userFriends).containsExactly(friend.getId());
        assertThat(friendFriends).isEmpty();
    }

    @Test
    void testRemoveFriend() {
        User friend = createUser("friend2@test.com", "friendlogin2");
        userStorage.addFriend(user.getId(), friend.getId());

        userStorage.removeFriend(user.getId(), friend.getId());

        assertThat(userStorage.getFriendIds(user.getId())).isEmpty();
    }

    @Test
    void testFriendshipConfirmation() {
        User friend = createUser("confirm@test.com", "confirmlogin");

        userStorage.addFriend(user.getId(), friend.getId());
        userStorage.addFriend(friend.getId(), user.getId());

        assertThat(userStorage.getFriendIds(user.getId())).contains(friend.getId());
        assertThat(userStorage.getFriendIds(friend.getId())).contains(user.getId());
    }

    private User createUser(String email, String login) {
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setLogin(login);
        newUser.setName("Name");
        newUser.setBirthday(LocalDate.of(1995, 5, 5));
        return userStorage.addUser(newUser);
    }
}
