package no.fint.zendesk.model.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZenDeskUserResponse {
    private ZenDeskUser user;
}