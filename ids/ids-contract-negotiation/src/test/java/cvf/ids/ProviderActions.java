package cvf.ids;

import cvf.ids.system.api.statemachine.ContractNegotiation;
import okhttp3.Response;

import java.util.Map;

import static cvf.ids.system.api.http.HttpFunctions.postJson;
import static cvf.ids.system.api.message.MessageFunctions.createOffer;
import static cvf.ids.system.api.message.MessageFunctions.createTermination;
import static cvf.ids.system.api.message.MessageFunctions.stringProperty;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;

/**
 *
 */
public class ProviderActions {
    private static final String NEGOTIATION_OFFER_TEMPLATE = "%s/negotiation/offer";

    public static void postOffer(Map<String, Object> contractRequest, ContractNegotiation negotiation) {
        var contractOffer = createOffer(negotiation.getId(), randomUUID().toString(), negotiation.getDatasetId());
        var callbackAddress = stringProperty("callbackAddress", contractRequest);

        try (var response = postJson(format(NEGOTIATION_OFFER_TEMPLATE, callbackAddress), contractOffer)) {
            checkResponse(response);
        }
    }

    public static void terminate(ContractNegotiation negotiation) {
        var termination = createTermination(negotiation.getId(),"1");
        try (var response = postJson(format(NEGOTIATION_OFFER_TEMPLATE, negotiation.getCallbackAddress()), termination)) {
            checkResponse(response);
        }
    }

    private static void checkResponse(Response response) {
        if (!response.isSuccessful()) {
            throw new AssertionError("Unexpected response code: " + response.code());
        }
    }

    private ProviderActions() {
    }
}
