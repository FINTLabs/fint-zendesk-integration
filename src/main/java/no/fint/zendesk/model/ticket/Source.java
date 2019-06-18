package no.fint.zendesk.model.ticket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Source {
    private String rel;
    private From from;
    private To to;
}
