package no.fint.zendesk.model.ticket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TicketCategoryOption {
    private String description;
    private String dn;
    private String basePath;
}
