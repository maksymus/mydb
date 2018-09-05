package org.mydb.command;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * lexer to be used by parse.
 * Lexer preprocesses sql to remove comments, classify chars etc before tokenizing
 */
public class Lexer {
    /** Low level char types assigned to each char in expression */
    enum CharType {
        NONE,           // space, comment etc
        NAME,           // name value (converts to IDENTIFIER token)
        NUMBER,         // number value
        STRING,         // string char i.e 'hello world'
        QT,             // string quote '\''
        EQT,            // escape quote - double quote
        SPECIAL1,       // special char *,+ etc
        SPECIAL2,       // special char !,>,< etc
        DOT,            // dot in float/alias
        END;            // end of stream
    }

    /** Original sql to parse */
    private String originalSql;

    /** Sql after preprocessing */
    private char[] preprocessedSql;

    /** Array of char types after preprocessing */
    private CharType[] charTypes;

    /** Position of next token */
    private int tokenReadPosition;

    /** Current token set after token read */
    private Token currentToken;

    /** Keywords */
    private static final Map<String, Token> keywords = new HashMap<>();

    static {
        try {
            Field[] fields = Token.class.getDeclaredFields();
            for (Field field : fields) {
                if (!Modifier.isStatic(field.getModifiers()))
                    continue;

                if (field.getType() == Token.class) {
                    Token token = (Token) field.get(null);
                    if (token.getTokenType() == Token.TokenType.KEYWORD) {
                        keywords.put(field.getName(), token);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new ParserException("failed to init lexer", e);
        }
    }

    public Lexer(String sql) {
        this.originalSql = sql;
        this.preprocessedSql = new char[sql.length() + 1];
        this.charTypes = new CharType[sql.length() + 1];

        preprocess();
    }

    public Token getCurrentToken() {
        return currentToken;
    }

    public Token getNextToken() {
        try {
            if (tokenReadPosition >= preprocessedSql.length)
                throw new ParserException("parse exception: token pointer past expression length");

            while (charTypes[tokenReadPosition] == CharType.NONE)
                tokenReadPosition++;

            CharType charType = this.charTypes[tokenReadPosition];
            Token token = getNextToken(charType);

            if (token != null)
                return (this.currentToken = token);

            throw new ParserException("unknown token at position " + tokenReadPosition);
        } catch (ParserException e) {
            tokenReadPosition = 0;
            currentToken = null;
            throw e;
        }
    }

    private Token getNextToken(CharType charType) {
        if (charType == CharType.SPECIAL1) {
            return tokenizeSpecial1(tokenReadPosition);
        } else if (charType == CharType.SPECIAL2) {
            return tokenizeSpecial2(tokenReadPosition);
        } else if (charType == CharType.NAME) {
            return tokenizeName(tokenReadPosition);
        } else if (charType == CharType.NUMBER) {
            return tokenizeNumber(tokenReadPosition);
        } else if (charType == CharType.DOT) {
            return tokenizeDot(tokenReadPosition);
        } else if (charType == CharType.QT) {
            return tokenizeString(tokenReadPosition);
        } else if (charType == CharType.END) {
            return tokenizeEnd();
        }

        return null;
    }

    /**
     * Preprocess sql before tokenizing.
     * Remove comment, and assign char types to each character.
     */
    private void preprocess() {
        int length = originalSql.length();

        originalSql.getChars(0, length, preprocessedSql, 0);

        for (int i = 0; i < length; i++) {
            CharType charType = CharType.NONE;

            char currChar = preprocessedSql[i];
            char nextChar = preprocessedSql[i+1];

            preprocessedSql[i] = currChar;

            if (currChar == '-' && nextChar == '-') {
                // -- comments
                i = preprocessDashComment(i);
            } else if (currChar == '\'') {
                // string
                charType = CharType.QT;
                i = preprocessString(i);
            } else if (currChar == '.') {
                charType = CharType.DOT;
            } else if (isSpecial1(currChar)) {
                charType = CharType.SPECIAL1;
            } else if (isSpecial2(currChar)) {
                charType = CharType.SPECIAL2;
            } else {
                if (Character.isWhitespace(currChar) || Character.isSpaceChar(currChar)) {
                    // whitespace
                    charType = CharType.NONE;
                } else if (currChar >= 'a' && currChar <= 'z') {
                    // lower case chars
                    preprocessedSql[i] = (char) (currChar - ('a' - 'A'));
                    charType = CharType.NAME;
                } else if (currChar >= 'A' && currChar <= 'Z') {
                    // upper case chars
                    charType = CharType.NAME;
                } else if (currChar >= '0' && currChar <= '9') {
                    // numbers
                    charType = CharType.NUMBER;
                } else {
                    charType = CharType.NAME;
                }
            }

            this.charTypes[i] = charType;
        }

        preprocessedSql[length] = ' ';
        charTypes[length] = CharType.END;
    }

    /**
     * Preprocess dash comment (-- ).
     * Removes comment.
     *
     * @param position Position of first dash
     * @return position after end of line
     */
    private int preprocessDashComment(int position) {
        while (true) {
            if (position >= originalSql.length())
                break;

            charTypes[position] = CharType.NONE;

            char command = preprocessedSql[position];
            preprocessedSql[position++] = ' ';

            if (command == '\n')
                break;
        }

        return position;
    }

    /**
     * Process strings.
     * @param position position of string start
     * @return position position of string end
     */
    private int preprocessString(int position) {
        charTypes[position] = CharType.QT;

        int strPosition = position + 1;

        while (true) {
            if (strPosition >= originalSql.length())
                throw new ParserException("closing quote missing for string");

            charTypes[strPosition] = CharType.STRING;

            char currChar = preprocessedSql[strPosition];
            char nextChar = preprocessedSql[strPosition + 1];

            if (currChar == '\'' && nextChar == '\'') {
                charTypes[strPosition] = CharType.EQT;
                charTypes[strPosition + 1] = CharType.EQT;
                strPosition++;
            } else if (currChar == '\'') {
                break;
            }

            strPosition++;
        }

        return strPosition;
    }

    /**
     * One char tokens.
     */
    private boolean isSpecial1(char ch) {
        switch (ch) {
            case '(':
            case ')':
            case '{':
            case '}':
            case '*':
            case '/':
            case ',':
            case ';':
            case '+':
            case '-':
            case '%':
            case '?':
            case '=':
                return true;
            default:
                return false;
        }
    }

    /**
     * Multi char tokens, i.e. >=, ||, etc
     */
    private boolean isSpecial2(char ch) {
        switch (ch) {
            case '!':
            case '<':
            case '>':
            case '|':
            case ':':
//            case '&':
//            case '~':
                return true;
            default:
                return false;
        }
    }

    private Token tokenizeSpecial1(int position) {
        char ch = preprocessedSql[position];

        tokenReadPosition = position + 1;

        switch (ch) {
            case '(': return Token.OPEN_PAREN;
            case ')': return Token.CLOSE_PAREN;
            case '{': return Token.OPEN_BRACE;
            case '}': return Token.CLOSE_BRACE;
            case '*': return Token.STAR;
            case '/': return Token.SLASH;
            case ',': return Token.COMA;
            case ';': return Token.SEMICOLON;
            case '+': return Token.PLUS;
            case '-': return Token.MINUS;
            case '%': return Token.PERCENT;
            case '?': return Token.QUESTION;
            case '=': return Token.EQUALS;
            case '<': return Token.LESS;
            case '>': return Token.MORE;
            case '!': return Token.NOTSIGN;
        }

        throw new ParserException("wrong special token at position: " + position);
    }

    private Token tokenizeSpecial2(int position) {
        char ch = preprocessedSql[position];

        // ignore spaces between special chars
        int nextPosition = position + 1;
        while (charTypes[nextPosition] == CharType.NONE)
            nextPosition++;

        char nextCh = preprocessedSql[nextPosition];

        tokenReadPosition = nextPosition + 1;

        if (ch == '!' && nextCh == '=') {
            return Token.NOT_EQUALS;
        } else if (ch == '<' && nextCh == '=') {
            return Token.LESS_EQUALS;
        } else if (ch == '>' && nextCh == '=') {
            return Token.MORE_EQUALS;
        } else if (ch == '|' && nextCh == '|') {
            return Token.STRING_CONCAT;
        } else if (ch == ':' && nextCh == '=') {
            return Token.COLUMN_EQUALS;
        }

        // try to find in special1 chars (to support <, > etc)
        return tokenizeSpecial1(position);
    }

    private Token tokenizeName(int position) {
        // keyword or identifier
        Token keyword = getKeyword(tokenReadPosition);

        if (keyword != null)
            return keyword;

        return getIdentifier(tokenReadPosition);
    }

    private Token tokenizeNumber(int position) {
        int start = position;
        int end = readWhile(position, CharType.NUMBER, CharType.DOT);

        tokenReadPosition = end;

        char[] valueChars = Arrays.copyOfRange(preprocessedSql, start, end);
        boolean hasDot = IntStream.range(start, end).anyMatch(i -> charTypes[i] == CharType.DOT);

        if (hasDot) {
            return new Token(Token.TokenType.VALUE, Double.valueOf(String.valueOf(valueChars)));
        }

        return new Token(Token.TokenType.VALUE, Integer.valueOf(String.valueOf(valueChars)));

    }

    private Token tokenizeDot(int position) {
        int start = position;
        int end = readWhile(position + 1, CharType.NONE);

        if (charTypes[end] == CharType.NUMBER) {
            int numberEnd = readWhile(end, CharType.NUMBER);
            char[] valueChars = Arrays.copyOfRange(preprocessedSql, end, numberEnd);

            tokenReadPosition = numberEnd;

            return new Token(Token.TokenType.VALUE, Double.valueOf("." + String.valueOf(valueChars)));
        }

        tokenReadPosition = position + 1;

        return Token.DOT;
    }

    private Token tokenizeString(int position) {
        StringBuilder sb = new StringBuilder();

        // pass quote
        int currentPosition = position + 1;

        while (true) {
            int end = readWhile(currentPosition, CharType.STRING);

            if (currentPosition < end) {
                char[] valueChars = Arrays.copyOfRange(preprocessedSql, currentPosition, end);
                sb.append(String.valueOf(valueChars));
                currentPosition = end;
            } else if (charTypes[end] == CharType.EQT && charTypes[end + 1] == CharType.EQT) {
                sb.append("'");
                currentPosition = currentPosition + 2;
            } else if (charTypes[end] == CharType.QT) {
                tokenReadPosition = end + 1;
                break;
            } else {
                throw new ParserException("closing quote missing for string");
            }
        }

        return new Token(Token.TokenType.VALUE, sb.length() > 0 ? sb.toString(): null);
    }

    private Token tokenizeEnd() {
        tokenReadPosition++;
        return Token.END;
    }

    private Token getIdentifier(int position) {
        int start = position;
        int end = readWhile(position, CharType.NAME, CharType.NUMBER);

        tokenReadPosition = end;

        char[] value = Arrays.copyOfRange(preprocessedSql, start, end);
        return new Token(Token.TokenType.IDENTIFIER, new String(value));
    }

    private Token getKeyword(int position) {
        int start = position;
        int end = readWhile(position, CharType.NAME, CharType.NUMBER);

        char[] value = Arrays.copyOfRange(preprocessedSql, start, end);

        Token token = keywords.get(String.valueOf(value).toUpperCase());
        if (token != null) {
            tokenReadPosition = end;
            return token;
        }

        return null;
    }

    private int readWhile(int start, CharType ... allowedCharTypes) {
        List<CharType> allowed = Arrays.asList(allowedCharTypes);

        int end = start;
        while (allowed.contains(charTypes[end]))
            end++;

        return end;
    }

    public static void main(String[] args) throws IllegalAccessException {
    }
}
