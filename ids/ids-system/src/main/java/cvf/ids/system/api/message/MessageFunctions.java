package cvf.ids.system.api.message;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static cvf.ids.system.api.message.IdsConstants.CONTEXT;
import static cvf.ids.system.api.message.IdsConstants.ID;
import static cvf.ids.system.api.message.IdsConstants.IDS_NAMESPACE;
import static cvf.ids.system.api.message.IdsConstants.ODRL_NAMESPACE;
import static cvf.ids.system.api.message.IdsConstants.TYPE;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Utility methods for creating IDS messages.
 */
public class MessageFunctions {

    public static Map<String, Object> createContractRequest(String processId, String offerId, String datasetId, String callbackAddress) {
        var message = createBaseMessage("ids:ContractRequestMessage");
        message.put(ID, processId); // override id
        message.put(CONTEXT, createContext());

        message.put("offerId", offerId);
        message.put("datasetId", datasetId);

        message.put("callbackAddress", callbackAddress);

        return message;
    }

    public static Map<String, Object> createContractCounterRequest(String processId, String datasetId) {
        var message = createBaseMessage("ids:ContractRequestMessage"); // do NOT override id
        message.put("processId", processId);
        message.put(CONTEXT, createContext());

        message.put("offer", createOffer(processId, UUID.randomUUID().toString(), datasetId));
        message.put("datasetId", datasetId);

        return message;
    }

    public static Map<String, Object> createTermination(String processId, String code, String... reasons) {
        var message = createBaseMessage("ids:ContractNegotiationTermination");
        message.put(CONTEXT, createContext());

        message.put("processId", processId);
        message.put("code", code);

        if (reasons != null && reasons.length > 0) {
            message.put("reasons", Arrays.stream(reasons).map(reason -> Map.of("message", reason)).collect(toList()));
        }
        return message;
    }

    public static Map<String, Object> createOffer(String processId, String offerId, String datasetId) {
        var message = createBaseMessage("ids:ContractOfferMessage");
        var context = createContext();
        context.put("odrl", ODRL_NAMESPACE);
        message.put(CONTEXT, context);
        message.put("ids:processId", processId);

        Map<String, Object> permissions = Map.of("action", "use", "constraints", emptyList());
        var offer = new LinkedHashMap<String, Object>();
        offer.put(TYPE, "odrl:Offer");
        offer.put("uid", offerId);
        offer.put("target", datasetId);
        offer.put("permissions", List.of(permissions));

        message.put("offer", offer);

        return message;
    }

    public static Map<String, Object> createNegotiationResponse(String id, String state) {
        var message = createBaseMessage("ids:ContractNegotiation");
        var context = createContext();
        message.put(CONTEXT, context);
        message.put(ID, id);
        message.put("ids:state", state);  // TODO JSON-LD
        return message;
    }

    public static Map<String, Object> mapProperty(String key, Map<String, Object> map) {
        var value = requireNonNull(map.get(key), "No value for: " + key);
        //noinspection unchecked
        return (Map<String, Object>) value;
    }

    public static String stringProperty(String key, Map<String, Object> map) {
        var value = requireNonNull(map.get(key), "No value for: " + key);
        return (String) value;
    }

    @NotNull
    private static Map<String, Object> createBaseMessage(String type) {
        var message = new LinkedHashMap<String, Object>();
        message.put(ID, UUID.randomUUID().toString());
        message.put(TYPE, type);
        return message;
    }

    private static Map<String, Object> createContext() {
        var context = new LinkedHashMap<String, Object>();
        context.put("ids", IDS_NAMESPACE);
        return context;
    }

    private MessageFunctions() {
    }
}
