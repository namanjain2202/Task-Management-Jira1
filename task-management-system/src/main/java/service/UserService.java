package service;

import model.User;
import repository.UserRepository;

public class UserService {
    private final UserRepository userRepo = new UserRepository();

    public User register(String name, String email, String password) {
        User user = new User(name, email, password);
        userRepo.save(user);
        return user;
    }

    public User login(String email, String password) {
        User user = userRepo.findByEmail(email);
        if (user != null && user.checkPassword(password)) {
            return user;
        }
        throw new RuntimeException("Invalid credentials!");
    }
}
