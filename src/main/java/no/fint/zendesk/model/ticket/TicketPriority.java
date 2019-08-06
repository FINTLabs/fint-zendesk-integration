package no.fint.zendesk.model.ticket;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TicketPriority {
    private String name;
    private String value;
    private String help;
}
