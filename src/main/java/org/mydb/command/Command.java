package org.mydb.command;

import org.mydb.engine.Session;

/**
 * Sql command
 */
public class Command {
    private Prepared prepared;
    private Session session;

    public Command(Session session, Prepared prepared) {
        this.prepared = prepared;
        this.session = session;

        this.prepared.setSession(session);
    }

    public void cancel() {
        // todo cancel command
    }
}
