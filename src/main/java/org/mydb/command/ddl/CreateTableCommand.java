package org.mydb.command.ddl;

import org.mydb.command.Prepared;
import org.mydb.engine.table.Table;

public class CreateTableCommand extends Prepared {
    private Table table;

    public CreateTableCommand(String originalSql) {
        super(originalSql);
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }
}
