package org.mydb.command.ddl;

import org.mydb.command.Prepared;
import org.mydb.table.Table;

public class CreateTableCommand implements Prepared {
    private Table table;

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }
}
