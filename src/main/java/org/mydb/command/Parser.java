package org.mydb.command;

import org.mydb.command.ddl.CreateTableCommand;
import org.mydb.command.dml.InsertOperation;
import org.mydb.command.dml.NoOperation;
import org.mydb.command.dml.SelectOperation;
import org.mydb.table.Column;
import org.mydb.table.Table;
import org.mydb.table.datatype.DataType;
import org.mydb.table.datatype.WithPrecision;
import org.mydb.table.datatype.WithScale;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Parser {
    private Lexer lexer;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    public Prepared parse() {
        Prepared result = null;

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
                return new NoOperation(); // no operation
        }

        if (result == null)
            throw new ParserException("wrong syntax");

        return result;
    }

    private Prepared parseCreate() {
        if (nextIf(Token.TABLE)) {
            return parseCreateTable();
        }

        return null;
    }

    private Prepared parseInsert() {
        return new InsertOperation();
    }

    private Prepared parseSelect() {
        return new SelectOperation();
    }

    private Prepared parseCreateTable() {
        Token<String> tableName = next(Token.TokenType.IDENTIFIER);
        CreateTableCommand createTableCommand = new CreateTableCommand();

        Table table = new Table();
        table.setName(tableName.getValue());

        createTableCommand.setTable(table);

        if (nextIf(Token.OPEN_PAREN)) {
            do {
                parseColumnDefinition(table);
            } while (nextIf(Token.COMA));

            next(Token.CLOSE_PAREN);
        }

        return createTableCommand;
    }

    private void parseColumnDefinition(Table table) {
        Column.ColumnBuilder columnBuilder = new Column.ColumnBuilder();

        // column name
        Token<String> columnName = next(Token.TokenType.IDENTIFIER);
        columnBuilder.setName(columnName.getValue());

        // data type
        Token<String> columnTypeToken = next(Token.TokenType.KEYWORD);
        String value = columnTypeToken.getValue();

        DataType dataType = DataType.getDataType(value);
        if (dataType == null) {
            throw new ParserException(String.format("data type not supported: %s", value));
        }
        columnBuilder.setDataType(dataType);

        if (dataType instanceof WithPrecision) {
            if (nextIf(Token.OPEN_PAREN)) {
                int precision = readInteger((i) -> i >= 0);
                columnBuilder.setPrecision(precision);

                if (dataType instanceof WithScale && nextIf(Token.COMA)) {
                    int scale = readInteger((i) -> i >= 0);
                    columnBuilder.setScale(scale);
                }

                next(Token.CLOSE_PAREN);
            }
        }

        table.addColumn(columnBuilder.build());
    }

    // low level parser commands ======================================================================================
    private Token next() {
        return lexer.getNextToken();
    }

    private Token next(Token ... tokens) {
        Token currentToken = lexer.getCurrentToken();

        boolean match = Arrays.stream(tokens).anyMatch(token -> token == currentToken);

        if (!match) {
            throw new ParserException(String.format("Expected %s but got %s",
                    Arrays.asList(tokens).stream()
                            .map(t -> String.valueOf(t.getValue()))
                            .collect(Collectors.joining(", ")),
                    currentToken.getValue()));
        }

        lexer.getNextToken();

        return currentToken;
    }

    private boolean nextIf(Token ... tokens) {
        Token currentToken = lexer.getCurrentToken();

        boolean match = Arrays.stream(tokens).anyMatch(token -> token == currentToken);

        if (match)
            lexer.getNextToken();

        return match;
    }

    private Token next(Token.TokenType ... tokenTypes) {
        Token currentToken = lexer.getCurrentToken();

        boolean match = Arrays.stream(tokenTypes).anyMatch(tokenType ->
                tokenType == currentToken.getTokenType());

        if (!match) {
            throw new ParserException(String.format("Expected %s but got %s",
                    Arrays.asList(tokenTypes).stream()
                            .map(t -> String.valueOf(t))
                            .collect(Collectors.joining(", ")),
                    currentToken.getValue()));
        }

        lexer.getNextToken();

        return currentToken;
    }

    private boolean nextIf(Token.TokenType ... tokenTypes) {
        Token currentToken = lexer.getCurrentToken();

        boolean match = Arrays.stream(tokenTypes).anyMatch(token ->
                token == currentToken.getTokenType());

        if (match)
            lexer.getNextToken();

        return match;
    }

    private int readInteger() {
        return readInteger(null);
    }

    private int readInteger(Predicate<Integer> restrict) {
        Token<Integer> number = next(Token.TokenType.VALUE);
        Integer value = number.getValue();

        if (restrict != null && !restrict.test(value)) {
            throw new ParserException(String.format("invalid number: %d", value));
        }

        return value;
    }
}
