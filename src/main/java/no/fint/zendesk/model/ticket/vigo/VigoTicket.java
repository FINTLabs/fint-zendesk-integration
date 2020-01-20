package no.fint.zendesk.model.ticket.vigo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.fint.zendesk.model.ticket.Comment;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VigoTicket {

    @NotNull
    private String subject;

    @NotNull
    private Organisation organisation;

    @NotNull
    private VigoUser vigoUser;

    @NotNull
    private String type;

    @NotNull
    private String priority;

    @NotNull
    private List<String> tags;

    @NotNull
    private Comment comment;
}
