package org.javalabs.tools.cbs2sql.cli.util;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import org.javalabs.tools.cbs2sql.cli.model.Transform;

/**
 *
 * @author schan280
 */
public final class GeneralUtility {

    public static final String TS_REGEXP = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])[ T]([01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d\\.{0,1}\\d{0,}Z?$";
    public static final String DT_REGEXP = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$";

    public static int rankOf(Class<?> clazz) {
        if (clazz == Byte.class || clazz == byte.class) {
            return 1;
        }
        else if (clazz == Short.class || clazz == short.class) {
            return 2;
        }
        else if (clazz == Integer.class || clazz == int.class) {
            return 3;
        }
        else if (clazz == Long.class || clazz == long.class) {
            return 4;
        }
        else if (clazz == Float.class || clazz == float.class) {
            return 2;
        }
        else if (clazz == Long.class || clazz == long.class) {
            return 4;
        }
        else if (clazz == Double.class || clazz == double.class) {
            return 5;
        }
        throw new IllegalArgumentException("Invalid primitive type: " + clazz);
    }

    public static Boolean isPrimitiveNumeric(Class<?> type) {
        return type == Integer.class
                || type == Long.class
                || type == Double.class
                || type == Float.class
                || type == Short.class
                || type == Byte.class
                || type == byte.class
                || type == short.class
                || type == int.class
                || type == long.class
                || type == float.class
                || type == double.class;
    }

    public static Boolean isPrimitive(Class<?> type) {
        return type == String.class
                || type == Integer.class
                || type == Long.class
                || type == Double.class
                || type == Float.class
                || type == Byte.class
                || type == Character.class
                || type == Short.class
                || type == Boolean.class
                || Date.class.isAssignableFrom(type)
                || Number.class.isAssignableFrom(type)
                || type.isEnum()
                || type == boolean.class
                || type == byte.class
                || type == char.class
                || type == short.class
                || type == int.class
                || type == long.class
                || type == float.class
                || type == double.class;
    }

    public static Class<?> javaType(String name, JsonNode node) {
        if (node == null || node.isNull()) {
            if (name.endsWith("_date") || name.endsWith("_time") || name.endsWith("_dt")) {

                return Date.class;
            }
            if (name.endsWith("_timestamp") || name.endsWith("_at")) {
                return Timestamp.class;
            }
            return String.class;
        }
        if (node.isBoolean()) {
            return Boolean.class;
        }
        if (node.isShort()) {
            return Short.class;
        }
        if (node.isInt()) {
            return Integer.class;
        }
        if (node.isLong()) {
            return Long.class;
        }
        if (node.isBigInteger()) {
            return BigInteger.class;
        }
        if (node.isFloat()) {
            return Float.class;
        }
        if (node.isDouble()) {
            return Double.class;
        }
        if (node.isBigDecimal()) {
            return BigDecimal.class;
        }
        if (node.isTextual()) {
            String s = node.asText();

            if (looksLikeTimestamp(s)) {
                return Timestamp.class;
            }
            if (looksLikeDate(s)) {
                return Date.class;
            }
            return String.class;
        }
        return null;
    }

    private static boolean looksLikeDate(String s) {
        return s != null && s.matches("^\\d{4}-\\d{2}-\\d{2}$");
    }

    private static boolean looksLikeTimestamp(String s) {
        return s != null && (s.matches("^\\d{4}-\\d{2}-\\d{2}T.*$")
                || s.matches("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}.*$"));
    }

    public static File[] listEligibleDocs(Transform payload) {
        File[] files = null;

        if (payload.getDirectory() != null) {
            File dir = new File(payload.getDirectory());
            if (!dir.exists()) {
                throw new IllegalArgumentException("Directory " + payload.getDirectory() + " does not exist");
            }
            files = dir.listFiles((File dir1, String name) -> {
                if (payload.getFilename() != null) {
                    if (payload.getFilename().charAt(0) == '*' && payload.getFilename().charAt(1) == '.') {
                        return name.endsWith(payload.getFilename().substring(1));
                    } else {
                        return name.equals(payload.getFilename());
                    }
                } else {
                    return Boolean.TRUE;
                }
            });
        } else {
            // Only filename is provided.
            files = new File[]{new File(payload.getFilename())};
        }
        Arrays.sort(files, (Object o1, Object o2) -> o1.toString().compareTo(o2.toString()));

        return files;
    }
}
