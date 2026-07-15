package org.javalabs.tools.cbs2sql.cli.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.io.IOException;

/**
 * Utility class providing a shared Jackson {@link ObjectMapper} instance and
 * convenience methods for JSON serialization and deserialization.
 *
 * <p>
 * The utility exposes methods for encoding Java objects to JSON,
 * decoding JSON into Java objects, and producing human-readable
 * (pretty-printed) JSON output.
 *
 * <p>
 * Any {@link IOException} encountered during serialization or
 * deserialization is wrapped in a {@link RuntimeException}.
 *
 * @author Sudiptasish Chanda
 */
public class MapperUtil {
    
    /**
     * Shared {@link ObjectMapper} instance used for JSON serialization and
     * deserialization.
     */
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    
    /**
     * Shared {@link ObjectWriter} configured to produce pretty-printed JSON.
     */
    private static final ObjectWriter JSON_PRETTY_MAPPER = new ObjectMapper().writerWithDefaultPrettyPrinter();
    
    /**
     * Deserializes the supplied JSON bytes into an instance of the specified
     * class.
     *
     * @param <T> the target type
     * @param buff the JSON content to deserialize
     * @param clazz the target class
     * @return the deserialized object
     * @throws RuntimeException if the JSON cannot be deserialized
     */
    public static <T> T decode(byte[] buff, Class<T> clazz) {
        try {
            return (T)mapper().readValue(buff, clazz);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Deserializes the supplied JSON bytes into a generic type.
     *
     * <p>This overload is intended for deserializing parameterized types
     * using Jackson's {@link TypeReference}.
     *
     * @param <T> the target type
     * @param buff the JSON content to deserialize
     * @param type an instance used only to infer the target generic type
     * @return the deserialized object
     * @throws RuntimeException if the JSON cannot be deserialized
     */
    public static <T> T decode(byte[] buff, T type) {
        try {
            return (T)mapper().readValue(buff, new TypeReference<T>() {});
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Serializes the supplied object into its JSON byte representation.
     *
     * @param obj the object to serialize
     * @return the serialized JSON bytes
     * @throws RuntimeException if the object cannot be serialized
     */
    public static byte[] encode(Object obj) {
        try {
            return mapper().writeValueAsBytes(obj);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the shared {@link ObjectMapper} instance.
     *
     * @return the shared JSON object mapper
     */
    public static ObjectMapper mapper() {
        return JSON_MAPPER;
    }

    /**
     * Serializes the supplied object into formatted (pretty-printed) JSON.
     *
     * @param obj the object to serialize
     * @return the pretty-printed JSON as a byte array
     * @throws RuntimeException if the object cannot be serialized
     */
    public static byte[] prettyWrite(Object obj) {
        try {
            return JSON_PRETTY_MAPPER.writeValueAsBytes(obj);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

