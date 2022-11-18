package cvf.core.api.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utility methods for JSON serialization.
 */
public class SerializationFunctions {
    private static ObjectMapper MAPPER = new ObjectMapper();

    public static String serialize(Object object) {
        try {
            return MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private SerializationFunctions() {
    }
}
