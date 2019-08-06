package no.fint.zendesk.model.ticket;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TicketType {
    private String name;
    private String value;
    private String help;
}
