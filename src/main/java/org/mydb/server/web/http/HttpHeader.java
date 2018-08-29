package org.mydb.server.web.http;


import org.mydb.util.StringUtils;

import java.util.Objects;

/**
 * Parsed header.
 * TODO split header value based on header type i.e. Content-Type: text/html;charset=utf-8
 */
public class HttpHeader {
    private String name = "";
    private String value = "";

    public HttpHeader(String name, String value) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(value);

        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        if (!StringUtils.isEmpty(name)) {
            return name + ": " + value;
        }

        return "";
    }
}
