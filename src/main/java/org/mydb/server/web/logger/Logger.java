package org.mydb.server.web.logger;

public class Logger {
    private Logger() {}

    public static Logger forClass(Class<?> clazz) {
        return new Logger();
    }

    public void error(String message, Throwable e) {
        System.out.println(message);
        e.printStackTrace();
    }

    public void trace(String message) {
        System.out.println(message);
    }

    public void warn(String message) {
        System.out.println(message);
    }

    public void warn(String message, Throwable e) {
        System.out.println(message);
        e.printStackTrace();
    }
}
