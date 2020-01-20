package no.fint.provisioning.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import no.fint.zendesk.model.ticket.vigo.VigoTicket;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Data
@RequiredArgsConstructor
public class RequestSynchronizationObject {

    private final VigoTicket ticket;
    private final AtomicInteger attempts = new AtomicInteger();
    private final String uuid = UUID.randomUUID().toString();
}