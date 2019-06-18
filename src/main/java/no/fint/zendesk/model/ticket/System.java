package no.fint.zendesk.model.ticket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class System {
    private double latitude;
    private String client;
    private String location;
    private String ipAddress;
    private double longitude;
}
