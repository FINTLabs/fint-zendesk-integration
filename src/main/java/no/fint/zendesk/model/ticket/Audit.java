package no.fint.zendesk.model.ticket;

import java.util.List;

public class Audit{
	private Metadata metadata;
	private String createdAt;
	private long id;
	private int ticketId;
	private long authorId;
	private List<EventsItem> events;
	private Via via;
}