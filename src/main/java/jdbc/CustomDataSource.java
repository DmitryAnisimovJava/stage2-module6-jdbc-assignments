package jdbc;

import lombok.Getter;
import lombok.Setter;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

@Getter
@Setter
public class CustomDataSource implements DataSource {

    private static volatile CustomDataSource instance;
    private static final CustomConnector connector = new CustomConnector();
    private final String driver;
    private final String url;
    private final String name;
    private final String password;

    private CustomDataSource(String driver, String url, String name, String password) {
        this.driver = driver;
        this.url = url;
        this.name = name;
        this.password = password;
    }

    public static CustomDataSource getInstance() {
        if (instance != null) {
            return instance;
        }
        synchronized (CustomDataSource.class) {
            if (instance != null) {
                return instance;
            }
            Properties properties = new Properties();
            try (InputStream streamProperties = CustomDataSource.class.getClassLoader()
                    .getResourceAsStream("app.properties")) {
                properties.load(streamProperties);
                String driver = properties.getProperty("postgres.driver");
                String url = properties.getProperty("postgres.url");
                String password = properties.getProperty("postgres.password");
                String name = properties.getProperty("postgres.name");
                instance = new CustomDataSource(driver, url, name, password);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return instance;
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return connector.getConnection(this.url, this.name, this.password);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return connector.getConnection(this.url, username, password);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return DriverManager.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        DriverManager.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        DriverManager.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (this.isWrapperFor(iface)) {
            return (T) this;
        }
        throw new SQLException("Can`t unwrap to %s not assignable class".formatted(iface));
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface != null && iface.isAssignableFrom(this.getClass());
    }
}
