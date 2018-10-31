package org.mydb.command;

import org.mydb.engine.Session;

/**
 * Parser factory
 */
public class ParserFactory {
    public Parser newParser(Session session, String sql) {
        return new Parser(session, new Lexer(sql));
    }
}
