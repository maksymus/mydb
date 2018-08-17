package org.dashdb.web.http;

import org.dashdb.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * HTTP request.
 */
public class HttpRequest {

    private HttpRequestLine httpRequestLine;
    private Map<String, HttpHeader> httpHeaders = new HashMap<>();
    Map<String, String> formData = new HashMap<>();

    public HttpRequestLine getHttpRequestLine() {
        return httpRequestLine;
    }

    public void setHttpRequestLine(HttpRequestLine httpRequestLine) {
        this.httpRequestLine = httpRequestLine;
    }

    public Map<String, HttpHeader> getHttpHeaders() {
        return httpHeaders;
    }

    public void setHttpHeaders(Map<String, HttpHeader> httpHeaders) {
        this.httpHeaders = httpHeaders;
    }

    public Map<String, String> getFormData() {
        return formData;
    }

    public void setFormData(Map<String, String> formData) {
        this.formData = formData;
    }

    public boolean hasHeader(String headerName) {
        return httpHeaders.get(headerName.toLowerCase()) != null;
    }

    public boolean hasHeader(String headerName, String headerValue) {
        HttpHeader httpHeader = httpHeaders.get(headerName.toLowerCase());
        return httpHeader != null && StringUtils.equalsEgnoreCase(httpHeader.getValue(), headerValue);
    }

    /**
     * HTTP request line.
     * example: GET /index.html HTTP/1.1
     */
    public static class HttpRequestLine {
        private HttpMethod httpMethod;
        private String uri;
        private String httpVersion;

        public HttpRequestLine(HttpMethod httpMethod, String uri, String httpVersion) {
            this.httpMethod = httpMethod;
            this.uri = uri;
            this.httpVersion = httpVersion;
        }

        public HttpMethod getHttpMethod() {
            return httpMethod;
        }

        public String getUri() {
            return uri;
        }

        public String getHttpVersion() {
            return httpVersion;
        }
    }

    /**
     * HTTP method.
     */
    public enum HttpMethod {
        GET, POST;

        public static HttpMethod value(String str) {
            for (HttpMethod method: HttpMethod.values()) {
                if (Objects.equals(method.name(), str)) {
                    return method;
                }
            }

            return null;
        }
    }
}
