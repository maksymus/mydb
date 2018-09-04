package org.mydb.server.web;

import org.mydb.server.web.logger.Logger;
import org.mydb.util.IOUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * WEB server.
 * Opens server socket, accepts connections and delegates request handling to WebThread.
 *
 * NIO non blocking server.
 * Accepts connections, switches channels to blocking mode when ready to read and  back to non blocking when
 * request processing is done.
 */
public class WebServer {
    public static final Logger logger = Logger.forClass(WebServer.class);

    public static final int PORT = 9090;
    public static final int N_THREADS_IN_POOL = 20;
    public static final int SOCKET_READ_TIME_OUT_SEC = 10;

    private ExecutorService webThreadExecutorService = Executors.newFixedThreadPool(N_THREADS_IN_POOL);
    private ExecutorCompletionService<SocketChannel> executorCompletionService =
            new ExecutorCompletionService<>(webThreadExecutorService);

    private ServerSocketChannel serverChannel;
    private Selector selector;

    public void start() {
        try {
            selector = Selector.open();

            serverChannel = ServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress(PORT));
            serverChannel.configureBlocking(false);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (true) {
                keepAliveChannels();
                cleanUpTimedOutChannels();

                // block for 10 ms then select
                if (selector.select(10) == 0)
                    continue;

                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> selectionKeyIterator = selectionKeys.iterator();

                while (selectionKeyIterator.hasNext()) {
                    SelectionKey selectionKey = selectionKeyIterator.next();
                    selectionKeyIterator.remove();

                    if (selectionKey.isAcceptable()) {
                        accept(selectionKey);
                    } else if (selectionKey.isReadable()) {
                        read(selectionKey);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("failed to start web server", e);
        }
    }

    private void accept(SelectionKey selectionKey)  {
        try {
            // accept connection
            ServerSocketChannel serverChannel = (ServerSocketChannel) selectionKey.channel();
            SocketChannel clientChannel = serverChannel.accept();
            clientChannel.configureBlocking(false);

            // register with selector and set access time to track time outs
            SelectionKey clientKey = clientChannel.register(selector, SelectionKey.OP_READ);
            clientKey.attach(LocalDateTime.now());
        } catch (IOException e) {
            logger.error("failed to accept", e);
        }
    }

    private void read(SelectionKey selectionKey) {
        // reset last access time
        selectionKey.attach(LocalDateTime.now());

        try {
            SocketChannel clientChannel = (SocketChannel) selectionKey.channel();

            // cancel to switch to blocking
            selectionKey.cancel();
            clientChannel.configureBlocking(true);

            executorCompletionService.submit(() -> {
                Socket socket = clientChannel.socket();
                socket.setSoTimeout(SOCKET_READ_TIME_OUT_SEC * 1000);

                WebThread webThread = new WebThread(socket);
                boolean keepAlive = webThread.run();

                if (keepAlive)
                    return clientChannel;

                IOUtils.close(clientChannel);
                return null;
            });
        } catch (IOException e) {
            logger.error("failed to read", e);
        }
    }

    /**
     * Check executorCompletionService completed pool and process completed channels.
     */
    private void keepAliveChannels() throws IOException {
        Future<SocketChannel> completedWebThread;
        while ((completedWebThread = executorCompletionService.poll()) != null) {
            SocketChannel clientChannel = null;
            try {
                clientChannel = completedWebThread.get();

                // if connection: keep-alive
                if (clientChannel != null) {
                    clientChannel.configureBlocking(false);
                    clientChannel.register(selector, SelectionKey.OP_READ);
                }
            } catch (InterruptedException | ExecutionException e) {
                logger.error("execution error", e);
                IOUtils.close(clientChannel);
            }
        }
    }

    private void cleanUpTimedOutChannels() {
        Set<SelectionKey> keys = selector.keys();

        for (SelectionKey key : keys) {
            if (key.isValid() && (key.interestOps() & SelectionKey.OP_READ) > 0) {
                Object attachment = key.attachment();
                if (attachment == null || !(attachment instanceof LocalDateTime))
                    continue;

                LocalDateTime lastAccessTime = (LocalDateTime) attachment;
                if (lastAccessTime.until(LocalDateTime.now(), ChronoUnit.SECONDS) > SOCKET_READ_TIME_OUT_SEC) {
                    key.cancel();
                    IOUtils.close(((SocketChannel) key.channel()).socket());
                    IOUtils.close(key.channel());
                }
            }
        }
    }

    public void stop() {
        IOUtils.close(selector);
        IOUtils.close(serverChannel);
    }

    public static void main(String[] args) {
        WebServer server = new WebServer();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.stop();
        }));

        server.start();
    }
}
