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
import static cvf.ids.system.api.message.IdsConstants.IDS_NAMESPACE_PREFIX;
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
        var message = createBaseMessage(IDS_NAMESPACE_PREFIX + "ContractRequestMessage");
        message.put(ID, processId); // override id
        message.put(CONTEXT, createContext());

        message.put(IDS_NAMESPACE_PREFIX + "offerId", offerId);
        message.put(IDS_NAMESPACE_PREFIX + "datasetId", datasetId);

        message.put(IDS_NAMESPACE_PREFIX + "callbackAddress", callbackAddress);

        return message;
    }

    public static Map<String, Object> createContractCounterOffer(String processId, String datasetId) {
        var message = createBaseMessage(IDS_NAMESPACE_PREFIX + "ContractRequestMessage"); // do NOT override id
        message.put(IDS_NAMESPACE_PREFIX + "processId", processId);
        message.put(CONTEXT, createContext());

        message.put(IDS_NAMESPACE_PREFIX + "offer", createOffer(processId, UUID.randomUUID().toString(), datasetId));
        message.put(IDS_NAMESPACE_PREFIX + "datasetId", datasetId);

        return message;
    }

    public static Map<String, Object> createTermination(String processId, String code, String... reasons) {
        var message = createBaseMessage(IDS_NAMESPACE_PREFIX + "ContractNegotiationTermination");
        message.put(CONTEXT, createContext());

        message.put(IDS_NAMESPACE_PREFIX + "processId", processId);
        message.put(IDS_NAMESPACE_PREFIX + "code", code);

        if (reasons != null && reasons.length > 0) {
            message.put(IDS_NAMESPACE_PREFIX + "reasons", Arrays.stream(reasons).map(reason -> Map.of("message", reason)).collect(toList()));
        }
        return message;
    }

    public static Map<String, Object> createAcceptedEvent(String processId) {
        return createEvent(processId, "accepted");
    }

    public static Map<String, Object> createFinalizedEvent(String processId) {
        return createEvent(processId, "finalized");
    }

    public static Map<String, Object> createEvent(String processId, String eventType) {
        var message = createBaseMessage(IDS_NAMESPACE_PREFIX + "ContractNegotiationEventMessage");
        message.put(CONTEXT, createContext());

        message.put(IDS_NAMESPACE_PREFIX + "processId", processId);
        message.put(IDS_NAMESPACE_PREFIX + "eventType", eventType);
        return message;
    }

    public static Map<String, Object> createVerification(String processId) {
        var message = createBaseMessage(IDS_NAMESPACE_PREFIX + "ContractAgreementVerificationMessage");
        message.put(CONTEXT, createContext());

        message.put(IDS_NAMESPACE_PREFIX + "processId", processId);
        return message;
    }

    public static Map<String, Object> createOffer(String processId, String offerId, String datasetId) {
        var message = createBaseMessage(IDS_NAMESPACE_PREFIX + "ContractOfferMessage");
        var context = createContext();
        context.put("odrl", ODRL_NAMESPACE);
        message.put(CONTEXT, context);
        message.put(IDS_NAMESPACE_PREFIX + "processId", processId);

        var permissions = Map.of("action", "use", "constraints", emptyList());
        var offer = new LinkedHashMap<String, Object>();
        offer.put(TYPE, "odrl:Offer");
        offer.put(ID, offerId);
        offer.put(IDS_NAMESPACE_PREFIX + "target", datasetId);
        offer.put(IDS_NAMESPACE_PREFIX + "permissions", List.of(permissions));

        message.put(IDS_NAMESPACE_PREFIX + "offer", offer);

        return message;
    }

    public static Map<String, Object> createAgreement(String processId, String agreementId, String datasetId) {
        var message = createBaseMessage(IDS_NAMESPACE_PREFIX + "ContractAgreementMessage");
        var context = createContext();
        context.put("odrl", ODRL_NAMESPACE);
        message.put(CONTEXT, context);
        message.put(IDS_NAMESPACE_PREFIX + "processId", processId);

        var permissions = Map.of("action", "use", "constraints", emptyList());
        var offer = new LinkedHashMap<String, Object>();
        offer.put(TYPE, "odrl:Agreement");
        offer.put(ID, agreementId);
        offer.put(IDS_NAMESPACE_PREFIX + "target", datasetId);
        offer.put(IDS_NAMESPACE_PREFIX + "permissions", List.of(permissions));

        message.put(IDS_NAMESPACE_PREFIX + "agreement", offer);

        return message;
    }

    public static Map<String, Object> createNegotiationResponse(String id, String state) {
        var message = createBaseMessage(IDS_NAMESPACE_PREFIX + "ContractNegotiation");
        var context = createContext();
        message.put(CONTEXT, context);
        message.put(ID, id);
        message.put(IDS_NAMESPACE_PREFIX + "state", state);  // TODO JSON-LD
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
