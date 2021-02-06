import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class DbHandler {
    private static final Logger LOG = LoggerFactory.getLogger(DbHandler.class);
    private static Connection conn;

    public static void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:" + DbHandler.class.getResource("authBD.db"));
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        LOG.debug("Соединение с БД установлено...");
    }

    public void registerNewUser(User user) {
        String insert = "INSERT INTO " + Constants.USER_TABLE + "(" + Constants.USER_FIRSTNAME + ", "
                + Constants.USER_LASTNAME + ", " + Constants.USER_USERNAME + "," + Constants.USER_PASSWORD + ") VALUES(?, ?, ?, ?);";
        try {
            PreparedStatement preparedStatement = conn.prepareStatement(insert);
            preparedStatement.setString(1, user.getFirstName());
            preparedStatement.setString(2, user.getLastName());
            preparedStatement.setString(3, user.getUserName());
            preparedStatement.setString(4, user.getPassword());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        LOG.debug("Добавлен новый пользователь {}, {}, {}, {}", user.getFirstName(), user.getLastName(), user.getUserName(), user.getPassword());
    }

    public ResultSet getUser(User user) {
        ResultSet rs = null;
        String select = "Select * FROM " + Constants.USER_TABLE + " WHERE " +
                Constants.USER_USERNAME + "=? AND " + Constants.USER_PASSWORD + "=?";
        try {
            PreparedStatement preparedStatement = conn.prepareStatement(select);
            preparedStatement.setString(1, user.getUserName());
            preparedStatement.setString(2, user.getPassword());
            rs = preparedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rs;
    }

    public static void close() {
        try {
            conn.close();
            LOG.debug("Соединение с БД закрыто");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public static Connection getConn() {
        return conn;
    }
}
