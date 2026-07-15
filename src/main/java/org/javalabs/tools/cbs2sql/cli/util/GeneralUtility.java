package org.javalabs.tools.cbs2sql.cli.util;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import org.javalabs.tools.cbs2sql.cli.model.Transform;

/**
 * Utility class containing helper methods for type inference, primitive type
 * inspection, numeric type ranking, date/timestamp detection, and document
 * discovery used by the CBS-to-SQL transformation process.
 *
 * <p>This class cannot be instantiated and provides only static utility
 * methods.
 *
 * @author schan280
 */
public final class GeneralUtility {

    /**
     * Regular expression used to identify timestamp strings in ISO-like formats.
     */
    public static final String TS_REGEXP = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])[ T]([01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d\\.{0,1}\\d{0,}Z?$";
    
    /**
     * Regular expression used to identify date strings in ISO-8601 format
     * (yyyy-MM-dd).
     */
    public static final String DT_REGEXP = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$";

    /**
     * Returns a precedence ranking for primitive numeric Java types.
     *
     * <p>The ranking can be used when determining the widest numeric type
     * required to represent multiple values.</p>
     *
     * @param clazz the primitive or wrapper numeric class
     * @return the numeric precedence rank
     * @throws IllegalArgumentException if the supplied class is not a supported
     *         primitive numeric type
     */
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

    /**
     * Determines whether the specified class represents a primitive numeric
     * type or its wrapper.
     *
     * @param type the class to evaluate
     * @return {@code true} if the class is a supported numeric primitive or
     *         wrapper; otherwise {@code false}
     */
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

    /**
     * Determines whether the specified class should be treated as a primitive
     * value during transformation.
     *
     * <p>This includes Java primitive types, their wrapper classes,
     * {@link String}, {@link Number} implementations, {@link java.util.Date}
     * types, and enumerations.</p>
     *
     * @param type the class to evaluate
     * @return {@code true} if the class is considered primitive for processing;
     *         otherwise {@code false}
     */
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

    /**
     * Returns all files that match the supplied transformation configuration.
     *
     * <p>If a directory is specified, files are filtered according to the
     * configured filename or wildcard pattern. If only a filename is provided,
     * that file is returned directly. The resulting array is sorted
     * alphabetically.</p>
     *
     * @param payload the transformation configuration
     * @return an alphabetically sorted array of eligible files
     * @throws IllegalArgumentException if the configured directory does not
     *         exist
     */
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
