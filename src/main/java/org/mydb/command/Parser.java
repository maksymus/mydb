package org.mydb.command;

public class Parser {
    private Lexer lexer;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    public Object parse() {
        Token token = lexer.getNextToken();

        Object result = null;

        switch (token.getTokenType()) {
            case KEYWORD:
                if (token == Token.CREATE) {
                    // parse create
                } else if (token == Token.INSERT) {
                    // parse insert
                } else if (token == Token.SELECT) {
                    // parse select
                }
            case END:
                result = new Object(); // no operation
        }

        if (result == null)
            throw new ParserException("wrong syntax");

        return result;
    }
}
