package org.dashdb.util;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;

public class StringUtils {

    /**
     * Check if string is <code>null</code>, empty or white space only.
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * Returns non null and trimmed string.
     */
    public static String empty(String str) {
        if (str == null)
            return "";

        return str.trim();
    }

    public static boolean equalsEgnoreCase(String str1, String str2) {
        if (str1 == null && str1 == null)
            return true;

        if (str1 == null || str2 == null)
            return false;

        return str1.equalsIgnoreCase(str2);
    }

    public static String urlDecode(String value) {
        int pos = 0;
        byte[] bytes = new byte[value.length()];

        for (int i = 0; i < value.length(); i++) {
            if ('+' == value.charAt(i)) {
                bytes[pos++] = ' ';
            } else if ('%' == value.charAt(i)) {
                int parsed = Byte.parseByte(value.substring(i + 1, i + 3), 16);
                bytes[pos++] = (byte) parsed;
                i += 2;
            } else {
                bytes[pos++] = (byte) value.charAt(i);
            }
        }

        return new String(bytes, 0, pos, StandardCharsets.UTF_8);
    }
}
