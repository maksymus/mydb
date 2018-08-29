package org.mydb.server.web.http;

/**
 * HTTP response.
 *
 * --------------------
 * | HTTP status code
 * --------------------
 * | HEADERS
 * --------------------
 * | CRLF
 * --------------------
 * | message
 * --------------------
 */
public class HttpResponse {
    private HttpStatusCode httpStatusCode;
    private byte[] message;
    private String contentType;

    public HttpStatusCode getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(HttpStatusCode httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public byte[] getMessage() {
        return message;
    }

    public void setMessage(byte[] message) {
        this.message = message;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public static class Builder {
        private HttpStatusCode httpStatusCode;
        private byte[] message;
        private String contentType;

        public Builder withHttpStatusCode(HttpStatusCode httpStatusCode) {
            this.httpStatusCode = httpStatusCode;
            return this;
        }

        public Builder withMessage(byte[] message) {
            this.message = message;
            return this;
        }

        public Builder withContentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public HttpResponse build() {
            HttpResponse httpResponse = new HttpResponse();
            httpResponse.setContentType(this.contentType);
            httpResponse.setHttpStatusCode(this.httpStatusCode);
            httpResponse.setMessage(this.message);
            return httpResponse;
        }

    }
}
