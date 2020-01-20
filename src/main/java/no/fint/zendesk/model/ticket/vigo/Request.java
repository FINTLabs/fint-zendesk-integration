package no.fint.zendesk.model.ticket.vigo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.fint.zendesk.model.ticket.Comment;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Request {

    @NotNull
    private Requester requester;

    @NotNull
    private String subject;

    @NotNull
    private String type;

    @NotNull
    private String priority;

    @NotNull
    private Comment comment;

    @NotNull
    private List<String> tags;
}
