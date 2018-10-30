package org.mydb.command.dml;

import org.mydb.command.Prepared;

public class SelectOperation extends Prepared {
    public SelectOperation(String originalSql) {
        super(originalSql);
    }
}
