package org.mydb.command.dml;

import org.mydb.command.Prepared;

public class InsertOperation extends Prepared {
    public InsertOperation(String originalSql) {
        super(originalSql);
    }
}
