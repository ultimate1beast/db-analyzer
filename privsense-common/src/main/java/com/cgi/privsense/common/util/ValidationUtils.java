package com.cgi.privsense.common.util;

import java.util.Map;

/**
 * Utility class for validating input parameters and data.
 */
public final class ValidationUtils {

    private ValidationUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Validates that an object is not null.
     *
     * @param obj the object to validate
     * @param message the error message if validation fails
     * @throws IllegalArgumentException if the object is null
     */
    public static void notNull(Object obj, String message) {
        if (obj == null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Validates that a string is not null or empty.
     *
     * @param str the string to validate
     * @param message the error message if validation fails
     * @throws IllegalArgumentException if the string is null or empty
     */
    public static void notEmpty(String str, String message) {
        if (str == null || str.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Validates that a map contains a specific key.
     *
     * @param map the map to check
     * @param key the key to look for
     * @param message the error message if validation fails
     * @throws IllegalArgumentException if the map doesn't contain the key
     */
    public static void containsKey(Map<String, ?> map, String key, String message) {
        notNull(map, "Map cannot be null");
        if (!map.containsKey(key)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Validates that a number is positive.
     *
     * @param number the number to validate
     * @param message the error message if validation fails
     * @throws IllegalArgumentException if the number is not positive
     */
    public static void positive(int number, String message) {
        if (number <= 0) {
            throw new IllegalArgumentException(message);
        }
    }
}
