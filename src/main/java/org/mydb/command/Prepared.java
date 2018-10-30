package org.mydb.command;

import org.mydb.engine.Session;

import java.util.Optional;

/**
 * Parsed DDL/DML sql statement
 */
public abstract class Prepared {
    /** original sql query */
    protected String originalSql;

    /** optional session - prepared statement can be detached */
    protected Optional<Session> session = Optional.empty();

    public Prepared(String originalSql) {
        this.originalSql = originalSql;
    }

    public Session getSession() {
        return session.orElse(null);
    }

    public void setSession(Session session) {
        this.session = Optional.of(session);
    }

    public String getOriginalSql() {
        return originalSql;
    }
}
