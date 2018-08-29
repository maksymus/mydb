package org.mydb.command;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;

import static org.mydb.command.Lexer.CharType.END;
import static org.mydb.command.Lexer.CharType.EQT;
import static org.mydb.command.Lexer.CharType.NAME;
import static org.mydb.command.Lexer.CharType.NONE;
import static org.mydb.command.Lexer.CharType.QT;
import static org.mydb.command.Lexer.CharType.SPECIAL1;
import static org.mydb.command.Lexer.CharType.STRING;
import static org.mydb.command.Token.DOT;
import static org.mydb.command.Token.FROM;
import static org.mydb.command.Token.LESS_EQUALS;
import static org.mydb.command.Token.MORE_EQUALS;
import static org.mydb.command.Token.SELECT;
import static org.mydb.command.Token.STAR;
import static org.mydb.command.Token.TokenType;
import static org.mydb.command.Token.WHERE;

public class LexerTest {
    @Test
    public void preprocess_dashComment() {
        String sql = "-- hello\r\n";

        Lexer lexer = new Lexer(sql);
        char[] preprocessedSql = getPreprocessedSql(lexer);
        Lexer.CharType[] charType = getCharTypes(lexer);

        Assert.assertArrayEquals("           ".toCharArray(), preprocessedSql);
        Assert.assertArrayEquals(new Lexer.CharType[] {
                NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, END
        }, charType);
    }

    @Test
    public void preprocess_string() {
        String sql = "'hello world'";

        Lexer lexer = new Lexer(sql);
        char[] preprocessedSql = getPreprocessedSql(lexer);
        Lexer.CharType[] charType = getCharTypes(lexer);

        Assert.assertArrayEquals("'hello world' ".toCharArray(), preprocessedSql);
        Assert.assertArrayEquals(new Lexer.CharType[] {
                QT, STRING, STRING, STRING, STRING, STRING, STRING, STRING, STRING, STRING, STRING, STRING, QT, END
        }, charType);
    }

    @Test
    public void preprocess_string_escape1() {
        String sql = "'a''b'";

        Lexer lexer = new Lexer(sql);
        char[] preprocessedSql = getPreprocessedSql(lexer);
        Lexer.CharType[] charType = getCharTypes(lexer);

        Assert.assertArrayEquals(new Lexer.CharType[] {
                QT, STRING, EQT, EQT, STRING, QT, END
        }, charType);
    }

    @Test
    public void preprocess_string_escape2() {
        String sql = "''";

        Lexer lexer = new Lexer(sql);
        Lexer.CharType[] charType = getCharTypes(lexer);

        Assert.assertArrayEquals(new Lexer.CharType[] {
                QT, QT, END
        }, charType);
    }

    @Test
    public void preprocess_string_escape3() {
        String sql = "''''''";

        Lexer lexer = new Lexer(sql);
        Lexer.CharType[] charType = getCharTypes(lexer);

        Assert.assertArrayEquals(new Lexer.CharType[] {
                QT, EQT, EQT, EQT, EQT, QT, END
        }, charType);
    }

    @Test
    public void preprocess_query1() {
        String sql = "select 'hello' FROM dual";

        Lexer lexer = new Lexer(sql);
        char[] preprocessedSql = getPreprocessedSql(lexer);
        Lexer.CharType[] charType = getCharTypes(lexer);

        Assert.assertArrayEquals("SELECT 'hello' FROM DUAL ".toCharArray(), preprocessedSql);
        Assert.assertArrayEquals(new Lexer.CharType[] {
            NAME, NAME, NAME, NAME, NAME, NAME,                     // SELECT
            NONE,                                                   // _space_
            QT, STRING, STRING, STRING, STRING, STRING, QT,         // 'hello'
            NONE,                                                   // _space_
            NAME, NAME, NAME, NAME,                                 // FROM
            NONE,                                                   // _space_
            NAME, NAME, NAME, NAME,                                 // DUAL
            END                                                     // END
        }, charType);
    }

    @Test
    public void preprocess_query2() {
        String sql = "select * FROM dual";

        Lexer lexer = new Lexer(sql);
        char[] preprocessedSql = getPreprocessedSql(lexer);
        Lexer.CharType[] charType = getCharTypes(lexer);

        Assert.assertArrayEquals("SELECT * FROM DUAL ".toCharArray(), preprocessedSql);
        Assert.assertArrayEquals(new Lexer.CharType[] {
            NAME, NAME, NAME, NAME, NAME, NAME,                     // SELECT
            NONE,                                                   // _space_
            SPECIAL1,                                                // *
            NONE,                                                   // _space_
            NAME, NAME, NAME, NAME,                                 // FROM
            NONE,                                                   // _space_
            NAME, NAME, NAME, NAME,                                 // DUAL
            END                                                     // END
        }, charType);
    }

    @Test
    public void getNextToken_special1() {
        String sql = " * ";

        Lexer lexer = new Lexer(sql);
        Token token = lexer.getNextToken();

        Assert.assertEquals(Token.STAR, token);
    }

    @Test
    public void getNextToken_special2() {
        String sql = " >   = ";

        Lexer lexer = new Lexer(sql);
        Token token = lexer.getNextToken();

        Assert.assertEquals(MORE_EQUALS, token);
    }

    @Test
    public void getNextToken_tokenize1() {
        String sql = " select * from dual where 1 <= 2.2";
        Lexer lexer = new Lexer(sql);

        Token select = lexer.getNextToken();
        Assert.assertEquals(SELECT, select);

        Token star = lexer.getNextToken();
        Assert.assertEquals(STAR, star);

        Token from = lexer.getNextToken();
        Assert.assertEquals(FROM, from);

        Token identifier = lexer.getNextToken();
        Assert.assertEquals(Token.TokenType.IDENTIFIER, identifier.getTokenType());
        Assert.assertEquals("DUAL", identifier.getValue());

        Token where = lexer.getNextToken();
        Assert.assertEquals(WHERE, where);

        Token value1 = lexer.getNextToken();
        Assert.assertEquals(Token.TokenType.VALUE, value1.getTokenType());
        Assert.assertEquals(1, value1.getValue());

        Token lessEq = lexer.getNextToken();
        Assert.assertEquals(LESS_EQUALS, lessEq);

        Token value2 = lexer.getNextToken();
        Assert.assertEquals(Token.TokenType.VALUE, value2.getTokenType());
        Assert.assertEquals(2.2, value2.getValue());

        Token end = lexer.getNextToken();
        Assert.assertEquals(Token.END, end);
    }

    @Test
    public void getNextToken_dot1() {
        String sql = ".1";
        Lexer lexer = new Lexer(sql);

        Token token = lexer.getNextToken();
        Assert.assertEquals(TokenType.VALUE, token.getTokenType());
        Assert.assertEquals(0.1, token.getValue());
    }

    @Test
    public void getNextToken_dot2() {
        String sql = "mytable.mycolumn";
        Lexer lexer = new Lexer(sql);

        Token ident1 = lexer.getNextToken();
        Assert.assertEquals(TokenType.IDENTIFIER, ident1.getTokenType());
        Assert.assertEquals("MYTABLE", ident1.getValue());

        Token dot = lexer.getNextToken();
        Assert.assertEquals(DOT, dot);

        Token ident2 = lexer.getNextToken();
        Assert.assertEquals(TokenType.IDENTIFIER, ident2.getTokenType());
        Assert.assertEquals("MYCOLUMN", ident2.getValue());
    }

    @Test
    public void getNextToken_string1() {
        String sql = "''";
        Lexer lexer = new Lexer(sql);

        Token token = lexer.getNextToken();
        Assert.assertEquals(TokenType.VALUE, token.getTokenType());
        Assert.assertEquals(null, token.getValue());
    }

    @Test
    public void getNextToken_string2() {
        String sql = "'hello world'";
        Lexer lexer = new Lexer(sql);

        Token token = lexer.getNextToken();
        Assert.assertEquals(TokenType.VALUE, token.getTokenType());
        Assert.assertEquals("hello world", token.getValue());
    }

    @Test
    public void getNextToken_string3() {
        String sql = "''''";
        Lexer lexer = new Lexer(sql);

        Token token = lexer.getNextToken();
        Assert.assertEquals(TokenType.VALUE, token.getTokenType());
        Assert.assertEquals("'", token.getValue());
    }

    @Test
    public void getNextToken_string4() {
        String sql = "'''''test''s'''''";
        Lexer lexer = new Lexer(sql);

        Token token = lexer.getNextToken();
        Assert.assertEquals(TokenType.VALUE, token.getTokenType());
        Assert.assertEquals("''test's''", token.getValue());
    }

    private Lexer.CharType[] getCharTypes(Lexer lexer) {
        return (Lexer.CharType[]) Whitebox.getInternalState(lexer, "charTypes");
    }

    private char[] getPreprocessedSql(Lexer lexer) {
        return (char[]) Whitebox.getInternalState(lexer, "preprocessedSql");
    }
}