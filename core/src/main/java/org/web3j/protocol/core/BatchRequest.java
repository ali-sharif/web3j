package org.web3j.protocol.core;

import org.web3j.protocol.Web3jService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.unmodifiableList;

/**
 * Rationale for not including the <code>List<Request<?, ? extends Response<?>>> requests</code> as
 * part of this object's state: we want the user to remember the requests
 *
 * Assumption - Sadly not enforced in the library :(
 * 1. Requests are immutable
 *
 */
public class BatchRequest {

    private final Web3jService service;
    private List<Request<?, ? extends Response<?>>> requests;

    public BatchRequest(Web3jService service) {
        this.service = service;
        requests = new ArrayList<>();
    }

    public void add(Request<?, ? extends Response<?>> request) {
        requests.add(request);
    }

    public void addAll(List<Request<?, ? extends Response<?>>> requests) {
        this.requests.addAll(requests);
    }

    public Optional<BatchResponse> execute() throws IOException {
        return service.sendBatch(unmodifiableList(new ArrayList<>(requests)));
    }
}
