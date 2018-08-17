package org.dashdb.web.http;

public enum MimeType {
    ICO("ico", "image/x-icon"),
    GIF("gif", "image/gif"),
    CSS("css", "text/css"),
    HTML("html", "text/html"),
    JS("js", "text/javascript"),
    OSTREAM("", "application/octet-stream"),
//    FORM("", "application/x-www-form-urlencoded");
    ;

    private final String extension;
    private final String type;

    MimeType(String extension, String type) {
        this.extension = extension;
        this.type = type;
    }

    public String getExtension() {
        return extension;
    }

    public String getType() {
        return type;
    }
}
