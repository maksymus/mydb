package org.mydb.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Table.
 */
public class Table {
    private String name;
    private List<Column> columns = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Column> getColumns() {
        return Collections.unmodifiableList(columns);
    }

    public void addColumn(Column column) {
        boolean columnExists = columns.stream().anyMatch(c -> Objects.equals(c.getName(), column.getName()));

        if (columnExists) {
            throw new TableException(String.format("column name exists %s", column.getName()));
        }

        columns.add(column);
    }
}
