package org.web3j.protocol;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.web3j.protocol.core.BatchResponse;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.websocket.events.Notification;
import org.web3j.utils.Async;
import rx.Observable;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Base service implementation.
 */
public abstract class Service implements Web3jService {

    protected final ObjectMapper objectMapper;

    public Service(boolean includeRawResponses) {
        objectMapper = ObjectMapperFactory.getObjectMapper(includeRawResponses);
    }

    protected abstract InputStream performIO(String payload) throws IOException;

    /*
    // Simple implementation
    public static class BatchRequest {
        private List<Request<?, ? extends Response<?>>> requests;
        private Map<Request<?, ? extends Response<?>>, Response<?>> responses;

        public BatchRequest() {
            requests = new ArrayList<>();
            responses = new HashMap<>();
        }

        public void add(Request<?, ? extends Response<?>> request) {
            requests.add(request);
        }

        public void addAll(List<Request<?, ? extends Response<?>>> requests) {
            this.requests.addAll(requests);
        }

        public <T extends Response<?>> Optional<T> getResponse(Request<?, T> request) {
            return Optional.ofNullable(request.getResponseType().cast(responses.get(request)));
        }
    }*/

    public Optional<BatchResponse> sendBatch(List<Request<?, ? extends Response<?>>> requests) throws IOException {

        String payload = objectMapper.writeValueAsString(requests);

        Map<Request<?, ? extends Response<?>>, Response<?>> responses = new HashMap<>();

        try (InputStream result = performIO(payload)) {
            if (result != null) {
                List<JsonNode> jsonResponse = objectMapper.readValue(result, new TypeReference<List<JsonNode>>(){});
                for (int i = 0; i < jsonResponse.size(); i++) {
                    responses.put(requests.get(i), objectMapper.treeToValue(jsonResponse.get(i),
                            requests.get(i).getResponseType()));
                }

                return Optional.of(new BatchResponse(responses));
            }
        }

        return Optional.empty();
    }


    @Override
    public <T extends Response> T send(
            Request request, Class<T> responseType) throws IOException {
        String payload = objectMapper.writeValueAsString(request);

        try (InputStream result = performIO(payload)) {
            if (result != null) {
                return objectMapper.readValue(result, responseType);
            } else {
                return null;
            }
        }
    }

    @Override
    public <T extends Response> CompletableFuture<T> sendAsync(
            Request jsonRpc20Request, Class<T> responseType) {
        return Async.run(() -> send(jsonRpc20Request, responseType));
    }

    @Override
    public <T extends Notification<?>> Observable<T> subscribe(
            Request request,
            String unsubscribeMethod,
            Class<T> responseType) {
        throw new UnsupportedOperationException(
                String.format(
                        "Service %s does not support subscriptions",
                        this.getClass().getSimpleName()));
    }
}
