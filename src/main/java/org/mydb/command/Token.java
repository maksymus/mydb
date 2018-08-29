package org.mydb.command;

/**
 * Tokens
 */
class Token<T> {
    // end
    static final Token END = new Token(TokenType.END);

    // special
    static final Token OPEN_PAREN = new Token(TokenType.SPECIAL);       // (
    static final Token CLOSE_PAREN = new Token(TokenType.SPECIAL);      // )
    static final Token OPEN_BRACE = new Token(TokenType.SPECIAL);       // {
    static final Token CLOSE_BRACE = new Token(TokenType.SPECIAL);      // }
    static final Token STAR = new Token(TokenType.SPECIAL);             // *
    static final Token SLASH = new Token(TokenType.SPECIAL);            // /
    static final Token COMA = new Token(TokenType.SPECIAL);             // ,
    static final Token DOT = new Token(TokenType.SPECIAL);              // .
    static final Token SEMICOLON = new Token(TokenType.SPECIAL);        // ;
    static final Token PLUS = new Token(TokenType.SPECIAL);             // +
    static final Token MINUS = new Token(TokenType.SPECIAL);            // -
    static final Token PERCENT = new Token(TokenType.SPECIAL);          // %
    static final Token QUESTION = new Token(TokenType.SPECIAL);         // ?
    static final Token EQUALS = new Token(TokenType.SPECIAL);           // =
    static final Token NOTSIGN = new Token(TokenType.SPECIAL);          // !
    static final Token NOT_EQUALS = new Token(TokenType.SPECIAL);       // !=
    static final Token LESS = new Token(TokenType.SPECIAL);             // <
    static final Token LESS_EQUALS = new Token(TokenType.SPECIAL);      // <=
    static final Token MORE = new Token(TokenType.SPECIAL);             // >
    static final Token MORE_EQUALS = new Token(TokenType.SPECIAL);      // >=
    static final Token STRING_CONCAT = new Token(TokenType.SPECIAL);    // ||
    static final Token COLUMN_EQUALS = new Token(TokenType.SPECIAL);    // :=

    // keywords
    static final Token CREATE = new Token(TokenType.KEYWORD);
    static final Token DATE = new Token(TokenType.KEYWORD);
    static final Token FROM = new Token(TokenType.KEYWORD);
    static final Token INDEX = new Token(TokenType.KEYWORD);
    static final Token INSERT = new Token(TokenType.KEYWORD);
    static final Token INTO = new Token(TokenType.KEYWORD);
    static final Token KEY = new Token(TokenType.KEYWORD);
    static final Token NOT = new Token(TokenType.KEYWORD);
    static final Token NUMBER = new Token(TokenType.KEYWORD);
    static final Token PRIMARY = new Token(TokenType.KEYWORD);
    static final Token SELECT = new Token(TokenType.KEYWORD);
    static final Token TABLE = new Token(TokenType.KEYWORD);
    static final Token VALUES = new Token(TokenType.KEYWORD);
    static final Token VARCHAR = new Token(TokenType.KEYWORD);
    static final Token WHERE = new Token(TokenType.KEYWORD);

    enum TokenType {
        IDENTIFIER,             // table, column names etc
        VALUE,                  // number
        SPECIAL,                // special char (,),+,- etc
        KEYWORD,                // SELECT, INSERT etc
        END,                    // end of expression
    }

    private final TokenType tokenType;
    private final T value;

    private Token(TokenType tokenType) {
        this.tokenType = tokenType;
        this.value = null;
    }

    Token(TokenType tokenType, T value) {
        this.tokenType = tokenType;
        this.value = value;
    }

    TokenType getTokenType() {
        return tokenType;
    }

    T getValue() {
        return value;
    }
}
