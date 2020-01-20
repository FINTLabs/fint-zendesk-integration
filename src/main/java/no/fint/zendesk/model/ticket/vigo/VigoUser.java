package no.fint.zendesk.model.ticket.vigo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VigoUser {
    @NotNull
    private String firstName;

    @NotNull
    private String lastName;

    @NotNull
    private String mobileNumber;

    @NotNull
    private String mailAddress;
}