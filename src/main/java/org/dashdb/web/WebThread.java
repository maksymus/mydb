package org.dashdb.web;

import org.dashdb.web.http.HttpHeader;
import org.dashdb.web.http.HttpRequest;
import org.dashdb.web.http.HttpRequest.HttpMethod;
import org.dashdb.web.http.HttpRequest.HttpRequestLine;
import org.dashdb.web.http.HttpResponse;
import org.dashdb.web.http.HttpStatusCode;
import org.dashdb.web.http.MimeType;
import org.dashdb.web.logger.Logger;
import org.dashdb.util.IOUtils;
import org.dashdb.util.StringUtils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Thread to handle each http request.
 */
public class WebThread implements Runnable {
    public static final Logger logger = Logger.forClass(WebServer.class);

    private Socket clientSocket;
    private InputStream inputStream;
    private OutputStream outputStream;

    private List<Consumer<WebThread>> lifeCycleObservers = new ArrayList<>();

    public WebThread(Socket clientSocket) {
        this.clientSocket = clientSocket;

        try {
            this.inputStream = clientSocket.getInputStream();
            this.outputStream = new BufferedOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException("failed to init web thread", e);
        }
    }

    @Override
    public void run() {
        try {
            while (clientSocket != null) {
                if (!process()) break;
            }
        } catch (Throwable t) {
            HttpResponse httpResponse = new HttpResponse.Builder()
                    .withHttpStatusCode(HttpStatusCode.INTERNAL_SERVER_ERROR)
                    .build();
            write(httpResponse);
            throw t;
        } finally {
            stopWebThread();
        }
    }

    public void stop() {
        stopWebThread();
    }

    /**
     * Process client's HTTP request.
     *
     * @return true if client sends Connection: Keep-Alive
     */
    private boolean process() {
        boolean keepAlive = false;

        HttpResponse httpResponse = new HttpResponse.Builder()
                .withHttpStatusCode(HttpStatusCode.NOT_FOUND)
                .withMessage("file not found".getBytes())
                .build();

        HttpRequest httpRequest = parseRequest();
        HttpRequestLine requestLine = httpRequest.getHttpRequestLine();

        if (Arrays.asList(HttpMethod.GET, HttpMethod.POST).contains(requestLine.getHttpMethod())) {
            keepAlive = httpRequest.hasHeader("connection", "keep-alive");

            String uri = requestLine.getUri();
            byte[] bytes = readFile(uri);
            if (bytes != null) {
                String mimeType = getMimeType(getFileName(uri)).getType();

                httpResponse = new HttpResponse.Builder()
                        .withHttpStatusCode(HttpStatusCode.OK)
                        .withContentType(mimeType)
                        .withMessage(bytes)
                        .build();
            }
        } else {
            httpResponse = new HttpResponse.Builder()
                    .withHttpStatusCode(HttpStatusCode.BAD_REQUEST)
                    .withMessage("bad request".getBytes())
                    .build();
        }

        write(httpResponse);
        return keepAlive;
    }

    /**
     * Close client socket and notifies observers.
     */
    private void stopWebThread() {
        IOUtils.close(clientSocket);

        clientSocket = null;
        inputStream = null;
        outputStream = null;

        lifeCycleObservers.forEach(obs -> obs.accept(this));
    }

    /**
     * Read line from HTTP response.
     *
     * @return non null string.
     */
    private String readLine() {
        StringBuilder message = new StringBuilder();

        try {
            while (true) {
                int read = inputStream.read();

                // connection closed
                if (read == -1)
                    throw new RuntimeException("client closed connection");

                // line end
                if (read == Constants.CR)
                    break;

                // carrier return/line end
                if (read == Constants.CR) {
                    int next = inputStream.read();
                    if (next == Constants.LF)
                        break;

                    message.append((char) read);
                    message.append((char) next);
                } else {
                    message.append((char) read);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("failed to read line", e);
        }

        return message.toString();
    }

    /**
     * Write HTTP response.
     *
     * HTTP/1.1 200 OK
     * HEADERS
     * CLRF
     * BODY
     */
    private void write(HttpResponse httpResponse) {
        String CRLF = String.valueOf(new char[] {Constants.CR, Constants.LF});
        byte[] message = httpResponse.getMessage();

        String responseLine = createResponseLine(httpResponse.getHttpStatusCode()) + CRLF;
        String contentLength = message.length > 0 ? "Content-Length: " + message.length + CRLF : "";
        String contentType = !StringUtils.isEmpty(httpResponse.getContentType()) ?
                "Content-Type: " + httpResponse.getContentType() + CRLF : "";

        try {
            outputStream.write((responseLine).getBytes());
            outputStream.write((contentLength).getBytes());
            outputStream.write((contentType).getBytes());
            outputStream.write((CRLF).getBytes());
            outputStream.write(message);
            outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException("failed to write message", e);
        }
    }

    /**
     * Create http response line (i.e. "HTTP/1.1 200 OK")
     * @param statusCode http status code.
     */
    private String createResponseLine(HttpStatusCode statusCode) {
        return new StringBuilder()
                .append(Constants.HTTP_VERSION).append(" ")
                .append(statusCode.getCode()).append(" ")
                .append(statusCode.getMsg())
                .toString();
    }

    private HttpRequest parseRequest() {
        // read request line
        HttpRequestLine httpRequestLine = readRequestLine();

        if (httpRequestLine == null)
            throw new RuntimeException("invalid request");

        // read request headers
        Map<String, HttpHeader> headers = readRequestHeader();

        HttpRequest httpRequest = new HttpRequest();
        httpRequest.setHttpRequestLine(httpRequestLine);
        httpRequest.setHttpHeaders(headers);

        // read form data
        if (httpRequest.hasHeader("content-type", "application/x-www-form-urlencoded")) {
            HttpHeader contentLengthHeader = httpRequest.getHttpHeaders().get("content-length");
            Map<String, String> formData = readFormData(contentLengthHeader);
            httpRequest.setFormData(formData);
        }

        return httpRequest;
    }

    private HttpRequestLine readRequestLine() {
        String line = readLine();

        String[] requestLine = line.split(" ");

        if (requestLine.length != 3)
            return null;

        HttpMethod method = HttpMethod.value(requestLine[0]);
        if (method == null)
            return null;

        return new HttpRequestLine(method, requestLine[1], requestLine[2]);
    }

    private Map<String, HttpHeader> readRequestHeader() {
        Map<String, HttpHeader> headers = new HashMap<>();

        while (true) {
            String line = readLine();
            if (StringUtils.isEmpty(line))
                break;

            int firstIndexOf = line.indexOf(":");

            if (firstIndexOf > 0) {
                String name = line.substring(0, firstIndexOf).trim();
                String value = line.substring(firstIndexOf + 1, line.length()).trim();

                if (StringUtils.isNotEmpty(name)) {
                    HttpHeader httpHeader = new HttpHeader(name, value);
                    headers.put(httpHeader.getName().toLowerCase(), httpHeader);
                }
            }
        }

        return headers;
    }

    private Map<String, String> readFormData(HttpHeader contentLengthHeader) {
        HashMap<String, String> data = new HashMap<>();

        if (contentLengthHeader == null)
            return data;

        int length = 0;
        try {
            length = Integer.valueOf(contentLengthHeader.getValue());
        } catch (NumberFormatException e) {
            logger.warn("invalid header value");
        }

        if (length == 0)
            return data;

        byte[] bytes = new byte[length];
        try {
            inputStream.read();

            for (int position = 0; position < length;) {
                position = inputStream.read(bytes, position, length - position);
            }
        } catch (IOException e) {
            logger.warn("invalid header value", e);
        }

        String str = new String(bytes, Charset.defaultCharset());
        String[] pairs = str.split("&");
        for (String pair : pairs) {
            int i = pair.indexOf("=");
            String name = pair.substring(0, i);
            String value = pair.substring(i, pair.length());
            data.put(name, StringUtils.urlDecode(value));
        }

        return data;
    }

    public byte[] readFile(String file) {
        String fileName = getFileName(file);

        try {
            try (InputStream input = WebThread.class.getResourceAsStream(fileName)) {
                if (input == null)
                    return null;

                ByteArrayOutputStream output = new ByteArrayOutputStream();
                byte[] buffer = new byte[Constants.BUFFER_SIZE];

                for (int n = input.read(buffer); n != Constants.EOF; n = input.read(buffer)) {
                    output.write(buffer, 0, n);
                }

                return output.toByteArray();
            }
        } catch (IOException e) {
            logger.error("failed to read file", e);
            return null;
        }
    }

    private String getFileName(String file) {
        String uri = file;

        if ("/".equals(uri)) {
            return Constants.RESOURCE_PATH + "/index.html";
        }

        return Constants.RESOURCE_PATH + uri;
    }

    private MimeType getMimeType(String file) {
        int i = file.lastIndexOf(".");
        if (i == -1)
            return MimeType.OSTREAM;

        String extension = file.substring(i + 1, file.length());

        for (MimeType mimeType : MimeType.values()) {
            if (mimeType.getExtension().equalsIgnoreCase(extension)) {
                return mimeType;
            }
        }

        return MimeType.OSTREAM;
    }

    public void addLifeCycleObserver(Consumer<WebThread> consumer) {
        lifeCycleObservers.add(consumer);
    }
}

