package no.fint.zendesk;

import lombok.extern.slf4j.Slf4j;
import no.fint.provisioning.model.TicketSynchronizationObject;
import no.fint.zendesk.model.ticket.Ticket;
import no.fint.zendesk.model.ticket.TicketRequest;
import no.fint.zendesk.model.ticket.TicketResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class ZenDeskTicketService {

    @Autowired
    private WebClient webClient;


    public Ticket createTicket(TicketSynchronizationObject ticketSynchronizationObject) {
        Ticket ticket = ticketSynchronizationObject.getTicket();
        log.debug("Creating ticket.");
        log.debug("\tAttempt: {}", ticketSynchronizationObject.getAttempts());
        TicketResponse ticketResponse = webClient.post()
                .uri("tickets.json")
                .syncBody(new TicketRequest(ticket))
                .retrieve()
                .bodyToMono(TicketResponse.class)
                .onErrorResume(response -> {
                    if (response instanceof WebClientResponseException) {
                        log.info("\t> Body: {}", ((WebClientResponseException) response).getResponseBodyAsString());
                    }
                    return Mono.error(response);
                })
                .block();

        return ticketResponse.getTicket();
    }
}
