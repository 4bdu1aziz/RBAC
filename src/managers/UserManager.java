package src.managers;
import models.User;
import src.filters.UserFilter;
import src.repositories.Repository;

import java.util.*;
import java.util.stream.Collectors;

public class UserManager implements Repository<User> {
    private final Map<String, User> usersByUsername;

    public UserManager() {
        this.usersByUsername = new HashMap<>();
    }

    @Override
    public void add(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        String username = user.username();
        if (usersByUsername.containsKey(username)) {
            throw new IllegalArgumentException("User with username '" + username + "' already exists");
        }

        usersByUsername.put(username, user);
    }

    @Override
    public boolean remove(User user) {
        if (user == null) {
            return false;
        }
        return usersByUsername.remove(user.username()) != null;
    }

    @Override
    public Optional<User> findById(String id) {
        // В User нет поля id, используем username как идентификатор
        return findByUsername(id);
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(usersByUsername.values());
    }

    @Override
    public int count() {
        return usersByUsername.size();
    }

    @Override
    public void clear() {
        usersByUsername.clear();
    }

    public Optional<User> findByUsername(String username) {
        if (username == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(usersByUsername.get(username));
    }

    public Optional<User> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return usersByUsername.values().stream()
                .filter(user -> user.email().equals(email))
                .findFirst();
    }

    public List<User> findByFilter(UserFilter filter) {
        if (filter == null) {
            return findAll();
        }
        return usersByUsername.values().stream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    public List<User> findAll(UserFilter filter, Comparator<User> sorter) {
        List<User> result = findByFilter(filter);
        if (sorter != null) {
            result.sort(sorter);
        }
        return result;
    }

    public boolean exists(String username) {
        if (username == null) {
            return false;
        }
        return usersByUsername.containsKey(username);
    }

    public void update(String username, String newFullName, String newEmail) {
        if (username == null) {
            throw new IllegalArgumentException("Username cannot be null");
        }

        User existingUser = usersByUsername.get(username);
        if (existingUser == null) {
            throw new IllegalArgumentException("User with username '" + username + "' not found");
        }

        String updatedFullName = newFullName != null ? newFullName : existingUser.fullName();
        String updatedEmail = newEmail != null ? newEmail : existingUser.email();

        User updatedUser = User.validate(username, updatedFullName, updatedEmail);
        usersByUsername.put(username, updatedUser);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserManager that = (UserManager) o;
        return Objects.equals(usersByUsername, that.usersByUsername);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usersByUsername);
    }
}