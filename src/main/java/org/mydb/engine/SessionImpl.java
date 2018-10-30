package org.mydb.engine;

public class SessionImpl implements Session {
    private boolean closed;

    public boolean isClosed() {
        return closed;
    }
}
