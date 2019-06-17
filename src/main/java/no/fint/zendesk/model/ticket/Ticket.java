package no.fint.zendesk.model.ticket;

import java.util.List;

public class Ticket{
	private String subject;
	private List<Object> emailCcIds;
	private String createdAt;
	private String description;
	private Object externalId;
	private Object type;
	private Via via;
	private boolean allowAttachments;
	private String updatedAt;
	private Object problemId;
	private List<Object> followerIds;
	private Object dueAt;
	private int id;
	private Object assigneeId;
	private String rawSubject;
	private Object forumTopicId;
	private List<CustomFieldsItem> customFields;
	private boolean allowChannelback;
	private Object satisfactionRating;
	private long submitterId;
	private String priority;
	private List<Object> collaboratorIds;
	private String url;
	private List<String> tags;
	private long brandId;
	private List<Object> sharingAgreementIds;
	private long groupId;
	private Object organizationId;
	private List<Object> followupIds;
	private Object recipient;
	private boolean isPublic;
	private boolean hasIncidents;
	private List<FieldsItem> fields;
	private String status;
	private long requesterId;
}