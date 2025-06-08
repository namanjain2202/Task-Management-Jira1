package repository;

import model.User;

import java.util.HashMap;
import java.util.Map;

public class UserRepository {
    private final Map<String, User> users = new HashMap<>();

    public void save(User user) {
        users.put(user.getId(), user);
    }

    public User findByEmail(String email) {
        return users.values().stream().filter(u -> u.getEmail().equals(email)).findFirst().orElse(null);
    }

    public User findById(String id) {
        return users.get(id);
    }
}