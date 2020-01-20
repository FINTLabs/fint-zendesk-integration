package no.fint.zendesk;

import lombok.extern.slf4j.Slf4j;
import no.fint.provisioning.model.RequestSynchronizationObject;
import no.fint.zendesk.model.ticket.Ticket;
import no.fint.zendesk.model.ticket.vigo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
public class ZebDeskRequestService {
    @Autowired
    private WebClient webClient;


    public Ticket createTicket(RequestSynchronizationObject requestSynchronizationObject) {
        VigoTicket ticket = requestSynchronizationObject.getTicket();
        log.debug("Creating ticket.");
        log.debug("\tAttempt: {}", requestSynchronizationObject.getAttempts());
        RequestResponse requestResponse = webClient.post()
                .uri("requests.json")
                .syncBody(
                        new VigoRequest(Request.builder()
                                .requester(Requester.builder()
                                        .name(ticket.getVigoUser().getFirstName() + ' ' + ticket.getVigoUser().getLastName())
                                        .email(ticket.getVigoUser().getMailAddress())
                                        .build())
                                .subject(ticket.getSubject())
                                .type(ticket.getType())
                                .priority(ticket.getPriority())
                                .comment(ticket.getComment())
                                .tags(ticket.getTags())
                                .build())
                )
                .retrieve()
                .bodyToMono(RequestResponse.class)
                .block();

        return requestResponse.getRequest();
    }
}
