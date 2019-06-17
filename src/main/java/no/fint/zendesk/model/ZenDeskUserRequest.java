package no.fint.zendesk.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZenDeskUserRequest {
    private ZenDeskUser user;
}
