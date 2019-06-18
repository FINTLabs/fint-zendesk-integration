package no.fint.provisioning.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import no.fint.portal.model.contact.Contact;
import no.fint.zendesk.model.ticket.Ticket;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

@Data
@RequiredArgsConstructor
public class TicketSynchronizationObject implements Serializable {

    private final Ticket ticket;
    private final AtomicInteger attempts = new AtomicInteger();
}
