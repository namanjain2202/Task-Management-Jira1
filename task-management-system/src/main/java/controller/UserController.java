package controller;

import model.User;
import service.UserService;

public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    public User register(String name, String email, String password) {
        try {
            return userService.register(name, email, password);
        } catch (Exception e) {
            System.out.println("Error registering user: " + e.getMessage());
            return null;
        }
    }

    public User login(String email, String password) {
        try {
            return userService.login(email, password);
        } catch (Exception e) {
            System.out.println("Error logging in: " + e.getMessage());
            return null;
        }
    }
}
