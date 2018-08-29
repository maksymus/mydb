package org.mydb.server.web;

import org.mydb.server.web.logger.Logger;
import org.mydb.util.IOUtils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * WEB server.
 * Opens server socket, accepts connections and delegates request handling to WebThread.
 */
public class WebServer {
    public static final Logger logger = Logger.forClass(WebServer.class);

    private ExecutorService webThreadExecutorService = Executors.newFixedThreadPool(10);
    public ServerSocket serverSocket;

    private Set<WebThread> webThreads = ConcurrentHashMap.newKeySet();

    public void start() {
        init();
        accept();
    }

    public void stop() {
        IOUtils.close(serverSocket);
        serverSocket = null;
    }

    private void init() {
        try {
            serverSocket = new ServerSocket(9090);
        } catch (IOException e) {
            throw new RuntimeException("failed to start server");
        }
    }

    private void accept() {
        while (serverSocket != null) {
            try {
                Socket clientSocket = serverSocket.accept();

                WebThread webThread = new WebThread(clientSocket);
                webThread.addLifeCycleObserver(this::onWebThreadStop);
                webThreads.add(webThread);

                webThreadExecutorService.submit(webThread);
            } catch (IOException e) {
                logger.error("failed to accept connection", e);
            }
        }
    }

    private void onWebThreadStop(WebThread webThread) {
        webThreads.remove(webThread);
    }

    public static void main(String[] args) {
        WebServer server = new WebServer();
        server.start();
    }
}
