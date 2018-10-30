package org.mydb.command.dml;

import org.mydb.command.Prepared;

public class NoOperation extends Prepared {
    public NoOperation(String originalSql) {
        super(originalSql);
    }
}
