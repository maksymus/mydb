package org.mydb.table;

import java.util.ArrayList;
import java.util.List;

/**
 * Table.
 */
public class Table {
    private String name;
    private List<Column> columns = new ArrayList<>();

    public void setName(String name) {
        this.name = name;
    }

    public void addColumn(Column column) {
        // todo check table name not exists
        columns.add(column);
    }
}
