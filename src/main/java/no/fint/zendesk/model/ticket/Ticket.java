package no.fint.zendesk.model.ticket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Ticket {
    @NotNull
    private String subject;

    @NotNull
    private String type;
    @NotNull
    private long submitterId;

    @NotNull
    private String priority;

    @NotNull
    private List<String> tags;

    @NotNull
    private long requesterId;

    @NotNull
    private Comment comment;

    private String createdAt;
    private String description;
    private long id;
    private long assigneeId;
    private String url;
    private String status;

    //private List<FieldsItem> fields;
    //private List<CustomFieldsItem> customFields;
    //private List<Long> emailCcIds;
    //private String externalId;
    //private Via via;
    //private boolean allowAttachments;
    //private String updatedAt;
    //private long problemId;
    //private List<Long> followerIds;
    //private Object dueAt;
    //private String rawSubject;
    //private Object forumTopicId;
    //private boolean allowChannelback;
    //private Object satisfactionRating;
    //private List<Object> collaboratorIds;
    //private long brandId;
    //private List<Object> sharingAgreementIds;
    //private long groupId;
    //private Object organizationId;
    //private List<Object> followupIds;
    //private Object recipient;
    //private boolean isPublic;
    //private boolean hasIncidents;
}