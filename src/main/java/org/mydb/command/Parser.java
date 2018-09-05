package org.mydb.command;

import java.util.Arrays;
import java.util.stream.Collectors;

public class Parser {
    private Lexer lexer;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    public Object parse() {
        Object result = null;

        Token token = next();
        switch (token.getTokenType()) {
            case KEYWORD:
                if (nextIf(Token.CREATE)) {
                    return parseCreate();
                } else if (nextIf(Token.INSERT)) {
                    return parseInsert();
                } else if (nextIf(Token.SELECT)) {
                    return parseSelect();
                }
            case END:
                result = new Object(); // no operation
        }

        if (result == null)
            throw new ParserException("wrong syntax");

        return result;
    }

    private Object parseCreate() {
        if (nextIf(Token.TABLE)) {
            return parseCreateTable();
        }

        return null;
    }

    private Object parseInsert() {
        return null;
    }

    private Object parseSelect() {
        return null;
    }

    private Object parseCreateTable() {
        return null;
    }

    private Token next() {
        return lexer.getNextToken();
    }

    private void next(Token ... tokens) {
        Token currentToken = lexer.getCurrentToken();

        boolean match = Arrays.stream(tokens).anyMatch(token ->
                token == currentToken || token.getTokenType() == currentToken.getTokenType());

        if (!match)
            throw new ParserException(String.format("Expected %s but got %s",
                    Arrays.asList(tokens).stream()
                            .map(t -> String.valueOf(t.getValue()))
                            .collect(Collectors.joining(", ")),
                    currentToken.getValue()));

        lexer.getNextToken();
    }

    private boolean nextIf(Token ... tokens) {
        Token currentToken = lexer.getCurrentToken();

        boolean match = Arrays.stream(tokens).anyMatch(token ->
                token == currentToken || token.getTokenType() == currentToken.getTokenType());

        if (match)
            lexer.getNextToken();

        return match;
    }
}
