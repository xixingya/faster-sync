package tech.xixing.sync.connector.mysql.source;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author liuzhifei
 * @since 1.0
 */
public class DataSource {

    private String host;
    private String username;

    private String password;

    private int port;

    private String databaseName;

    Connection connection;


    public DataSource(String host, String username, String password, int port) throws SQLException {
        this.username = username;
        this.password = password;
        this.port = port;
        this.databaseName = "*";
        this.host = host;
        initConnection();
    }

    public DataSource(String username, String password) {
        this.username = username;
        this.password = password;
        this.port = 3306;
        this.databaseName = "*";
    }

    public DataSource(String username, String password, int port, String databaseName) {
        this.username = username;
        this.password = password;
        this.port = port;
        this.databaseName = databaseName;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public Connection getConnection() {
        return connection;
    }

    private void initConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://" + host + ":" + port + "/?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true";
            connection = DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
