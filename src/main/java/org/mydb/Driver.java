package org.mydb;

import org.mydb.jdbc.JdbcConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * JDBC driver
 */
public class Driver implements java.sql.Driver {

    private static final Driver INSTANCE = new Driver();

    public synchronized static Driver load() {
        try {
            DriverManager.registerDriver(INSTANCE);
        } catch (SQLException e) {
            // todo add exception handling
            e.printStackTrace();
        }

        return INSTANCE;
    }

    public synchronized static void unload() {
        try {
            DriverManager.deregisterDriver(INSTANCE);
        } catch (SQLException e) {
            // todo add exception handling
            e.printStackTrace();
        }
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        if (!acceptsURL(url)) {
            return null;
        }

        return new JdbcConnection(url, info);
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        return url.toLowerCase().startsWith("jdbc:mdb:");
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        return new DriverPropertyInfo[0];
    }

    @Override
    public int getMajorVersion() {
        return 0;
    }

    @Override
    public int getMinorVersion() {
        return 1;
    }

    @Override
    public boolean jdbcCompliant() {
        return true;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }
}
