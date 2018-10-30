package org.mydb.server.web;

import java.sql.Connection;
import java.time.LocalDateTime;

/**
 * TODO web session - jsessionid
 * Stored in web server and holds user last connection/access/preferences etc.
 */
public class WebSession implements AutoCloseable {
    private Connection connection;
    private LocalDateTime lastAccessTime;

    public Connection getConnection() {
        if (connection == null) {
            // todo setup connection
        }

        return connection;
    }

    public LocalDateTime getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(LocalDateTime lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    @Override
    public void close() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }
}
