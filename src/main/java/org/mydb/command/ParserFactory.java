package org.mydb.command;

import org.mydb.engine.Session;
import org.mydb.util.factory.ObjectFactory;

/**
 * Parser factory
 */
public class ParserFactory implements ObjectFactory<Parser> {
    private Session session;
    private String sql;

    public ParserFactory(Session session) {
        this.session = session;
    }

    @Override
    public Parser getObject() {
        return new Parser(session, new Lexer(sql));
    }

    @Override
    public Class<Parser> getObjectClass() {
        return Parser.class;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }
}
