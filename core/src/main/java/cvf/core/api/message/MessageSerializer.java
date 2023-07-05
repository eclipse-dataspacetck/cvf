package cvf.core.api.message;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.JsonDocument;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsonp.JSONPModule;
import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;

import java.util.Map;

/**
 * Provides a configured {@link ObjectMapper} for serializing and deserializing JSON-LD messages.
 */
public class MessageSerializer {
    public static final JsonDocument EMPTY_CONTEXT = JsonDocument.of(JsonStructure.EMPTY_JSON_OBJECT);

    public static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper();
        MAPPER.registerModule(new JSONPModule());
        var module = new SimpleModule() {
            @Override
            public void setupModule(SetupContext context) {
                super.setupModule(context);
            }
        };
        MAPPER.registerModule(module);
    }

    public static String serialize(Object object) {
        try {
            return MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> compact(Map<String, Object> message) {
        var document = JsonDocument.of(MAPPER.convertValue(message, JsonObject.class));
        try {
            var compacted = JsonLd.compact(document, EMPTY_CONTEXT).get();
            return MAPPER.convertValue(compacted, Map.class);
        } catch (JsonLdError e) {
            throw new RuntimeException(e);
        }
    }

    private MessageSerializer() {
    }
}
