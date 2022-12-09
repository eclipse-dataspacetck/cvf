package cvf.ids.system.api.connector;

import cvf.ids.system.api.statemachine.ContractNegotiation;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static cvf.ids.system.api.message.IdsConstants.IDS_NAMESPACE;
import static cvf.ids.system.api.message.MessageFunctions.stringProperty;
import static cvf.ids.system.api.statemachine.ContractNegotiation.State.CONSUMER_AGREED;
import static cvf.ids.system.api.statemachine.ContractNegotiation.State.CONSUMER_REQUESTED;
import static cvf.ids.system.api.statemachine.ContractNegotiation.State.PROVIDER_OFFERED;
import static cvf.ids.system.api.statemachine.ContractNegotiation.State.TERMINATED;

/**
 * Manages contract negotiations on a consumer.
 */
public class ConsumerNegotiationManager {

    private Map<String, ContractNegotiation> negotiations = new ConcurrentHashMap<>();

    private Queue<ConsumerNegotiationListener> listeners = new ConcurrentLinkedQueue<>();

    public void consumerRequested(String processId, String correlationId) {
        var contractNegotiation = getNegotiations().get(processId);
        contractNegotiation.setCorrelationId(correlationId, CONSUMER_REQUESTED);
    }

    public void consumerCounterRequested(String processId) {
        var contractNegotiation = getNegotiations().get(processId);
        contractNegotiation.transition(CONSUMER_REQUESTED);
    }

    public void acceptLastOffer(String processId) {
        var contractNegotiation = getNegotiations().get(processId);
        contractNegotiation.transition(CONSUMER_AGREED);
    }

    public void terminate(String id) {
        var negotiation = getNegotiations().get(id);
        negotiation.transition(TERMINATED, n -> listeners.forEach(l -> l.terminated(n)));
    }

    public ContractNegotiation createNegotiation(String datasetId) {
        var negotiation = ContractNegotiation.Builder.newInstance().datasetId(datasetId).build();
        negotiations.put(negotiation.getId(), negotiation);

        listeners.forEach(l -> l.negotiationCreated(negotiation));

        return negotiation;
    }

    public void providerOffer(Map<String, Object> offer) {
        var id = stringProperty(IDS_NAMESPACE + "processId", offer);
        var negotiation = findByCorrelationId(id);
        negotiation.storeOffer(offer, PROVIDER_OFFERED);
    }

    public void handleAgreement(Map<String, Object> agreement) {
        var id = stringProperty(IDS_NAMESPACE + "processId", agreement);
        var negotiation = findByCorrelationId(id);
        negotiation.storeAgreement(agreement);
    }

    @NotNull
    public ContractNegotiation findByCorrelationId(String id) {
        return negotiations.values().stream()
                .filter(n -> id.equals(n.getCorrelationId()))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Negotiation not found for correlation id: " + id));
    }

    public Map<String, ContractNegotiation> getNegotiations() {
        return negotiations;
    }

    public void registerListener(ConsumerNegotiationListener listener) {
        listeners.add(listener);
    }

    public void deregisterListener(ConsumerNegotiationListener listener) {
        listeners.remove(listener);
    }

}
