package no.fint.zendesk.model.ticket.vigo;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class User {
    private String name;
    private String email;
    private boolean verified;
}