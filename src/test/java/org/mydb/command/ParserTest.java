package org.mydb.command;

import org.junit.Test;

public class ParserTest {

    @Test
    public void parse_create_table1() {
        Lexer lexer = new Lexer("CREATE TABLE MY_TABLE (\n" +
                "   ID   NUMBER(10),\n" +
                "   NAME VARCHAR(20)\n" +
                ");");

        Parser parser = new Parser(lexer);
        parser.parse();
    }
}
