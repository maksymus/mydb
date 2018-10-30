package org.mydb.engine;

public class Engine implements SessionFactory {
    private static final Engine INSTANCE = new Engine();

    private Engine() {}

    public static Engine getInstance() {
        return INSTANCE;
    }

    @Override
    public Session createSession() {
        return new SessionImpl();
    }
}
