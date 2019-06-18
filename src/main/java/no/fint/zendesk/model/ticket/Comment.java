package no.fint.zendesk.model.ticket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class Comment {
    @NotNull
    private String body;
//    private String subject;
//    private List<Long> recipients;
//    private long id;
//    private String type;
//    private Via via;
//    private String value;
//    private String fieldName;
//    private long auditId;
//    private List<Object> attachments;
//    private boolean jsonMemberPublic;
//    private String htmlBody;
//    private String plainBody;
//    private long authorId;
}