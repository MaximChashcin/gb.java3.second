package ru.geekbrains.chat_server.auth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PrimitiveInMemoryAuthService implements AuthService {

    private List<User> users;

    public PrimitiveInMemoryAuthService() {
        this.users = new ArrayList<>(
                Arrays.asList(
                        new User("user1", "log1", "pass"),
                        new User("user2", "log2", "pass"),
                        new User("user3", "log3", "pass")
                )
        );

    }

    @Override
    public boolean start() {
        System.out.println("Auth service started");
        return true;
    }

    @Override
    public void stop() {
        System.out.println("Auth service stopped");
    }

    @Override
    public boolean registerUser(String login, String password) {
        for (User user : users) {
            if (user.getLogin().equals(login))
                return false;
        }

        users.add(new User("user" + users.size(), login, password));
        return true;
    }

    @Override
    public String getUsernameByLoginAndPassword(String login, String password) {
        for (User user : users) {
            if (user.getLogin().equals(login) && user.getPassword().equals(password)) return user.getUsername();
        }
        return null;
    }

    @Override
    public boolean changeUsername(String oldName, String password, String newName) {
        return false;
    }

    @Override
    public boolean changePassword(String username, String newPassword) {
        return false;
    }
}
