package org.dashdb.util;

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
}
