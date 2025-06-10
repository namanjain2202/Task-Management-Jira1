package service;

import model.User;
import repository.UserRepository;
import exception.UserNotFoundException;

public class UserService {
    private final UserRepository userRepo = new UserRepository();

    public User register(String name, String email, String password) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        
        User existingUser = userRepo.findByEmail(email);
        if (existingUser != null) {
            throw new IllegalArgumentException("User with this email already exists");
        }
        
        User user = new User(name, email, password);
        userRepo.save(user);
        return user;
    }

    public User login(String email, String password) {
        User user = userRepo.findByEmail(email);
        if (user == null) {
            throw new UserNotFoundException("User not found with email: " + email);
        }
        if (!user.checkPassword(password)) {
            throw new IllegalArgumentException("Invalid password");
        }
        return user;
    }

    public User getUserById(String id) {
        User user = userRepo.findById(id);
        if (user == null) {
            throw new UserNotFoundException("User not found with id: " + id);
        }
        return user;
    }
}
