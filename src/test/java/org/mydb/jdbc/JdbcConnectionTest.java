package org.mydb.jdbc;

import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

public class JdbcConnectionTest {

    @Test
    public void createStatement_invoke() {
        JdbcConnection jdbcConnection = new JdbcConnection("", new Properties());
        Assert.assertNotNull(jdbcConnection);
    }
}