package org.dashdb.web.http;


import org.dashdb.util.StringUtils;

import java.util.Objects;

/**
 * Parsed header.
 */
public class HttpHeader {
    private String name = "";
    private String value = "";

    public HttpHeader(String line) {
        Objects.requireNonNull(line);

        String[] header = line.split(":");

        if (header.length == 2) {
            name = header[0].trim().toLowerCase();
            value = header[1].trim().toLowerCase();
        }
    }

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
