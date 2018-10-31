package org.mydb.jdbc;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.mydb.engine.Session;

import java.sql.ResultSet;
import java.sql.SQLException;

@RunWith(MockitoJUnitRunner.class)
public class JdbcStatementTest {
    @Mock
    private JdbcConnection jdbcConnectionMock;

    @Mock
    private Session session;

    @Test
    public void executeQuery_invoke() throws SQLException {
        Mockito.when(jdbcConnectionMock.getSession()).thenReturn(session);

        JdbcStatement jdbcStatement = new JdbcStatement(jdbcConnectionMock);
        ResultSet resultSet = jdbcStatement.executeQuery("select 1 from dual");
    }
}