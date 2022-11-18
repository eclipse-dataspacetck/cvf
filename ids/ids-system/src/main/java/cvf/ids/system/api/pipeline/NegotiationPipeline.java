package cvf.ids.system.api.pipeline;

import cvf.core.api.system.CallbackEndpoint;
import cvf.ids.system.api.client.NegotiationClient;
import cvf.ids.system.api.connector.Connector;
import cvf.ids.system.api.statemachine.ContractNegotiation;
import org.awaitility.core.ConditionTimeoutException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import static cvf.ids.system.api.message.IdsConstants.ID;
import static cvf.ids.system.api.message.MessageFunctions.createContractCounterRequest;
import static cvf.ids.system.api.message.MessageFunctions.createContractRequest;
import static cvf.ids.system.api.message.MessageFunctions.createTermination;
import static cvf.ids.system.api.message.MessageFunctions.stringProperty;
import static cvf.ids.system.api.statemachine.ContractNegotiation.State.TERMINATED;
import static java.util.UUID.randomUUID;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A test pipeline to create and execute message interaction tests.
 */
public class NegotiationPipeline {
    private static final int DEFAULT_WAIT_SECONDS = 15;

    private CallbackEndpoint endpoint;
    private Connector connector;
    private NegotiationClient negotiationClient;

    private long waitTime = DEFAULT_WAIT_SECONDS;
    private List<Runnable> stages = new ArrayList<>();

    private ContractNegotiation clientNegotiation;

    public static NegotiationPipeline negotiationPipeline(NegotiationClient negotiationClient, CallbackEndpoint endpoint, Connector connector) {
        var pipeline = new NegotiationPipeline();
        pipeline.negotiationClient = negotiationClient;
        pipeline.connector = connector;
        pipeline.endpoint = endpoint;
        return pipeline;
    }

    public NegotiationPipeline waitTime(long waitTime) {
        this.waitTime = waitTime;
        return this;
    }

    public NegotiationPipeline sendRequest() {
        stages.add(() -> {
            var datasetId = randomUUID().toString();
            var offerId = randomUUID().toString();
            clientNegotiation = connector.getConsumerNegotiationManager().createNegotiation(datasetId);

            var contractRequest = createContractRequest(clientNegotiation.getId(), offerId, datasetId, endpoint.getAddress());

            var response = negotiationClient.contractRequest(contractRequest);
            var correlationId = stringProperty(ID, response);
            connector.getConsumerNegotiationManager().consumerRequested(clientNegotiation.getId(), correlationId);
        });
        return this;
    }


    public NegotiationPipeline sendCounterRequest() {
        stages.add(() -> {
            var contractRequest = createContractCounterRequest(clientNegotiation.getCorrelationId(), clientNegotiation.getDatasetId());
            negotiationClient.contractRequest(contractRequest);
            connector.getConsumerNegotiationManager().consumerCounterRequested(clientNegotiation.getId());
        });
        return this;
    }

    public NegotiationPipeline sendTermination() {
        stages.add(() -> {
            var termination = createTermination(clientNegotiation.getCorrelationId(), "1");
            negotiationClient.terminate(termination);
            connector.getConsumerNegotiationManager().terminate(clientNegotiation.getId());
        });
        return this;
    }

    public NegotiationPipeline then(Runnable runnable) {
        stages.add(runnable);
        return this;
    }

    public NegotiationPipeline thenWaitForState(ContractNegotiation.State state) {
        return thenWait("state to transition to " + state, () -> state == clientNegotiation.getState());
    }

    public NegotiationPipeline thenWait(String description, Callable<Boolean> condition) {
        stages.add(() -> {
            try {
                await().atMost(waitTime, SECONDS).until(condition);
            } catch (ConditionTimeoutException e) {
                throw new AssertionError("Timeout waiting for " + description);
            }
        });
        return this;
    }

    public NegotiationPipeline expectOffer(Consumer<Map<String, Object>> action) {
        stages.add(() ->
                endpoint.registerHandler("negotiation/offer", offer -> {
                    //noinspection unchecked
                    action.accept((Map<String, Object>) offer);
                    endpoint.deregisterHandler("negotiation/offer");
                    return null;
                }));
        return this;
    }

    public NegotiationPipeline expectTermination() {
        stages.add(() ->
                endpoint.registerHandler("negotiation/offer", termination -> {
                    clientNegotiation.transition(TERMINATED);
                    endpoint.deregisterHandler("negotiation/offer");
                    return null;
                }));
        return this;
    }

    public NegotiationPipeline thenVerify(Runnable runnable) {
        return then(runnable);
    }

    public NegotiationPipeline thenVerifyNegotiation(Consumer<ContractNegotiation> consumer) {
        return then(() -> consumer.accept(clientNegotiation));
    }

    public NegotiationPipeline thenVerifyState(ContractNegotiation.State state) {
        stages.add(() -> assertEquals(state, clientNegotiation.getState()));
        return this;
    }

    public NegotiationPipeline thenVerifyProviderState(ContractNegotiation.State state) {
        stages.add(() -> {
            var providerNegotiation = negotiationClient.getNegotiation(clientNegotiation.getCorrelationId());
            assertEquals(state, ContractNegotiation.State.valueOf(stringProperty("ids:state", providerNegotiation).toUpperCase())); // TODO JSON-LD
        });
        return this;
    }

    public void execute() {
        stages.forEach(Runnable::run);
    }

}
