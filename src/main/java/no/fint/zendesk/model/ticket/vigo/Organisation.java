package no.fint.zendesk.model.ticket.vigo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Organisation {
    @NotNull
    private String name;
    @NotNull
    private String organisationNumber;
}