package ru.geekbrains.chat_server.auth;

public interface AuthService {
    boolean start();
    void stop();
    boolean registerUser(String login, String password);
    String getUsernameByLoginAndPassword(String login, String password);
    boolean changeUsername(String oldName, String password, String newName);
    boolean changePassword(String username, String password);
}
