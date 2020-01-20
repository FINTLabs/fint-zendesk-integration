package no.fint.zendesk.model.ticket.vigo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Requester {
    private String name;
    private String email;
}
