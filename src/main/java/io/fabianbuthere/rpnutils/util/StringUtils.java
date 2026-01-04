package io.fabianbuthere.rpnutils.util;

public class StringUtils {
    public static String title(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }
}
