package no.fint.zendesk.model.ticket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class From {
    private boolean deleted;
    private long id;
    private String title;
}
