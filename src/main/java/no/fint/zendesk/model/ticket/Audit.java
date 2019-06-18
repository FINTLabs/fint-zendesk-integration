package no.fint.zendesk.model.ticket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Audit {
    private Metadata metadata;
    private String createdAt;
    private long id;
    private int ticketId;
    private long authorId;
    private List<Comment> events;
    private Via via;
}