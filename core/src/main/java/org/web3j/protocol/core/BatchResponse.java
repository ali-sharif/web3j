package org.web3j.protocol.core;

import org.web3j.protocol.Web3jService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BatchResponse {
    private final Map<Request<?, ? extends Response<?>>, Response<?>> responses;

    public BatchResponse(Map<Request<?, ? extends Response<?>>, Response<?>> responses) {
        this.responses = responses;
    }

    public <T extends Response<?>> Optional<T> get(Request<?, T> request) {
        return Optional.ofNullable(request.getResponseType().cast(responses.get(request)));
    }
}
