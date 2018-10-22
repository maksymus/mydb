package org.mydb.command.ddl;

import org.mydb.command.Prepared;

public class CreateTableCommand implements Prepared {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
