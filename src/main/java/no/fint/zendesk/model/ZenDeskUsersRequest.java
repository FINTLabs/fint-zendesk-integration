package no.fint.zendesk.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZenDeskUsersRequest {
    private List<ZenDeskUser> users;
}
