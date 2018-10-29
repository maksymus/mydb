package org.mydb.command;

import org.junit.Test;
import org.mydb.command.ddl.CreateTableCommand;
import org.mydb.table.Column;
import org.mydb.table.Table;
import org.mydb.table.datatype.Number;
import org.mydb.table.datatype.Varchar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ParserTest {

    @Test
    public void parse_create_table1() {
        Lexer lexer = new Lexer("CREATE TABLE MY_TABLE (\n" +
                "   ID   NUMBER(10),\n" +
                "   NAME VARCHAR(20)\n" +
                ");");

        Parser parser = new Parser(lexer);
        CreateTableCommand createCommand = (CreateTableCommand) parser.parse();

        Table table = createCommand.getTable();

        assertNotNull(table);
        assertEquals("MY_TABLE", table.getName());
        assertEquals(2, table.getColumns().size());

        Column column1 = table.getColumns().get(0);
        Column column2 = table.getColumns().get(1);

        assertEquals("ID", column1.getName());
        assertEquals(Number.class, column1.getDataType().getClass());
        assertEquals(10, column1.getPrecision());
        assertEquals(0, column1.getScale());

        assertEquals("NAME", column2.getName());
        assertEquals(Varchar.class, column2.getDataType().getClass());
        assertEquals(20, column2.getPrecision());
    }
}
