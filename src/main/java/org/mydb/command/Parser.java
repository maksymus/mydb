package org.mydb.command;

import org.mydb.command.ddl.CreateTableCommand;
import org.mydb.command.dml.InsertOperation;
import org.mydb.command.dml.NoOperation;
import org.mydb.command.dml.SelectOperation;
import org.mydb.engine.Session;
import org.mydb.engine.table.Column;
import org.mydb.engine.table.Table;
import org.mydb.engine.table.datatype.DataType;
import org.mydb.engine.table.datatype.WithPrecision;
import org.mydb.engine.table.datatype.WithScale;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * SQL statement parser.
 */
public class Parser {

    /** Current session */
    private Session session;

    /** Lexer used by parser */
    private Lexer lexer;

    Parser(Session session, Lexer lexer) {
        this.session = session;
        this.lexer = lexer;
    }

    public Command command() {
        return new Command(session, parse());
    }

    /**
     * Parse statement to get prepared statement.
     */
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
                return new NoOperation(lexer.getOriginalSql()); // no operation
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
        return new InsertOperation(lexer.getOriginalSql());
    }

    private Prepared parseSelect() {
        return new SelectOperation(lexer.getOriginalSql());
    }

    private Prepared parseCreateTable() {
        Token<String> tableName = next(Token.TokenType.IDENTIFIER);
        CreateTableCommand createTableCommand = new CreateTableCommand(lexer.getOriginalSql());

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
