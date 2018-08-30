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
 */
public class WebServer {
    public static final Logger logger = Logger.forClass(WebServer.class);

    private ExecutorService webThreadExecutorService = Executors.newFixedThreadPool(20);
    private ExecutorCompletionService<SocketChannel> executorCompletionService =
            new ExecutorCompletionService<>(webThreadExecutorService);

    private ServerSocketChannel serverChannel;
    private Selector selector;

    public void start() {
        try {
            selector = Selector.open();

            serverChannel = ServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress(9090));
            serverChannel.configureBlocking(false);

            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (true) {
                keepAliveChannels();

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

    private void accept(SelectionKey selectionKey)  {
        try {
            ServerSocketChannel serverChannel = (ServerSocketChannel) selectionKey.channel();
            SocketChannel clientChannel = serverChannel.accept();
            clientChannel.configureBlocking(false);
            clientChannel.register(selector, SelectionKey.OP_READ);
        } catch (IOException e) {
            logger.error("failed to accept", e);
        }
    }

    private void read(SelectionKey selectionKey) {
        try {
            SocketChannel clientChannel = (SocketChannel) selectionKey.channel();

            // cancel to switch to blocking
            selectionKey.cancel();
            clientChannel.configureBlocking(true);

            executorCompletionService.submit(() -> {
                Socket socket = clientChannel.socket();

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
