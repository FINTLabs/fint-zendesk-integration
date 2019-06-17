package no.fint.zendesk.model.ticket;

import java.util.List;

public class EventsItem{
	private String subject;
	private List<Long> recipients;
	private long id;
	private String type;
	private String body;
	private Via via;
	private String value;
	private String fieldName;
	private long auditId;
	private List<Object> attachments;
	private boolean jsonMemberPublic;
	private String htmlBody;
	private String plainBody;
	private long authorId;
}