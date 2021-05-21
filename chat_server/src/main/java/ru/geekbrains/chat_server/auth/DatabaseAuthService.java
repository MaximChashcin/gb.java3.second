package ru.geekbrains.chat_server.auth;

import java.sql.*;

public class DatabaseAuthService implements AuthService {
    private Connection connection;
    private Statement statement;

    private PreparedStatement preparedRegister;
    private PreparedStatement preparedGetUser;
    private PreparedStatement preparedChangeLogin;
    private PreparedStatement preparedChangePassword;

    // QUESTION: длинный литерал, как праильнее его перенсти что бы влезать в 100 символов в строке?
    private String createDatabase = "create table if not exists users (id integer primary key autoincrement, login text, password text)";

    // QUESTION: у меня вызывает боль эти обьявления стрингов в начале класса,
    // кажется что они мешают чтению кода
    // это у меня диформация от с++ и в java это номально или есть способ лучше?
    // в с++ сделал бы при помощи локальной static переменной:

    /*
    bool register(std::string login, std::string pwd) {
        static std::string register = "insert into users (login, password) values (?, ?)";
        ....
    }
     */

    private String register = "insert into users (login, password) values (?, ?)";
    private String getUser = "select id from users where login = ? and password = ?";
    private String changeLogin = "update users set login = ? where login = ? and password = ?";
    private String changePassword = "update users set password = ? where login = ?";

    @Override
    public boolean start() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:users.db");

            statement = connection.createStatement();
            statement.execute(createDatabase);

            preparedRegister = connection.prepareStatement(register);
            preparedChangePassword = connection.prepareStatement(changePassword);
            preparedChangeLogin = connection.prepareStatement(changeLogin);
            preparedGetUser = connection.prepareStatement(getUser);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }

        // QUESTION: правильно я понял что prepareStatement это некая сущность на стороне БД
        // и проверки sql injection аттак о которых вы говорили происходят тоже на стороне БД?

        return true;
    }

    @Override
    public void stop() {
        try {
            if (preparedRegister != null)
                preparedRegister.close();
            if (preparedChangePassword != null)
                preparedChangePassword.close();
            if (preparedChangeLogin != null)
                preparedChangeLogin.close();
            if (preparedGetUser != null)
                preparedGetUser.close();

            if (statement != null)
                statement.close();

            if (connection != null)
                connection.close();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        // QUESTION: у нас тут маленькое приложение а набежало 4 prepareStatement
        // с которыми очень не удобно работать, нужно все закрыть, все открыть, а если из сильно больше?
        // может есть какой то удобный инструмент для работы с коллекциями prepareStatement?
    }

    @Override
    public boolean registerUser(String login, String password) {
        try {
            preparedRegister.setString(1, login);
            preparedRegister.setString(2, password);
            preparedRegister.execute();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public String getUsernameByLoginAndPassword(String login, String password) {
        try {
            preparedGetUser.setString(1, login);
            preparedGetUser.setString(2, password);
            ResultSet res = preparedGetUser.executeQuery();

            if(!res.next())
                return null;

            return "user"+res.getInt(1);

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean changeUsername(String login, String password, String newLogin) {
        try {
            preparedChangeLogin.setString(1, newLogin);
            preparedChangeLogin.setString(2, login);
            preparedChangeLogin.setString(3, password);
            return preparedGetUser.executeUpdate() == 1;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean changePassword(String login, String password) {
        try {
            preparedChangePassword.setString(1, password);
            preparedChangePassword.setString(2, login);
            return preparedChangePassword.executeUpdate() == 1;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
    }
}
