package org.mydb.engine.table;

public class TableException extends RuntimeException {
    public TableException() {
    }

    public TableException(String message) {
        super(message);
    }

    public TableException(String message, Throwable cause) {
        super(message, cause);
    }

    public TableException(Throwable cause) {
        super(cause);
    }
}
