package no.fint.provisioning.model;

import lombok.Builder;
import lombok.Data;
import no.fint.zendesk.model.ticket.Ticket;

@Data
@Builder
public class TicketStatus {

    public enum Status {
        CREATED,
        ERROR,
        RUNNING
    }

    private final Ticket ticket;
    private final Status status;


}
