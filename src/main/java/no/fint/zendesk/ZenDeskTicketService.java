package no.fint.zendesk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
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

    @Autowired
    private ObjectMapper objectMapper;

    public Ticket createTicket(Ticket ticket) {

        try {
            String s = objectMapper.writeValueAsString(new TicketRequest(ticket));
            log.info(s);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
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
