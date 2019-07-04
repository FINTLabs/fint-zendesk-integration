package no.fint.provisioning;

import lombok.extern.slf4j.Slf4j;
import no.fint.provisioning.model.TicketStatus;
import no.fint.provisioning.model.TicketSynchronizationObject;
import no.fint.zendesk.model.ticket.Ticket;
import no.fint.zendesk.model.ticket.TicketPriority;
import no.fint.zendesk.model.ticket.TicketType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.net.URI;
import java.util.Arrays;

@Slf4j
@RestController
@CrossOrigin
@RequestMapping("tickets")
public class TicketController {

    @Autowired
    private TicketQueuingService ticketQueuingService;

    @Autowired
    private StatusCache statusCache;

    @PostMapping
    public ResponseEntity createTicket(@RequestBody @Valid Ticket ticket, HttpServletRequest request) {
        TicketSynchronizationObject ticketSynchronizationObject = new TicketSynchronizationObject(ticket);
        ticketQueuingService.put(ticketSynchronizationObject);

        TicketStatus ticketStatus = TicketStatus.builder().status(TicketStatus.Status.RUNNING).ticket(ticket).build();
        statusCache.put(ticketSynchronizationObject.getUuid(), ticketStatus);


        URI location = UriComponentsBuilder.fromUriString(request.getRequestURL().toString())
                .path("/status/{id}")
                .buildAndExpand(ticketSynchronizationObject.getUuid())
                .toUri();
        return ResponseEntity.status(HttpStatus.ACCEPTED).location(location).build();
    }

    @GetMapping("/status/{id}")
    public ResponseEntity getStatus(@PathVariable String id) {

        log.debug("/status/{}", id);

        if (!statusCache.containsKey(id)) {
            return ResponseEntity.notFound().build();
        }

        TicketStatus ticketStatus = statusCache.get(id);

        if (ticketStatus.getStatus() == TicketStatus.Status.RUNNING) {
            return ResponseEntity.notFound().build();
        }

        if (ticketStatus.getStatus() == TicketStatus.Status.ERROR) {
            return ResponseEntity.unprocessableEntity().build();
        }

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ticketStatus.getTicket());

    }

    // TODO: 2019-06-18 Get the codes from the ZenDesk api
    @GetMapping("type")
    public ResponseEntity getTicketTypes() {
        return ResponseEntity.ok(
                Arrays.asList(
                        TicketType.builder().name("Spørsmål").value("question").build(),
                        TicketType.builder().name("Hendelse").value("incident").build(),
                        TicketType.builder().name("Problem").value("problem").build(),
                        TicketType.builder().name("Oppgave").value("task").build()
                )
        );
    }

    // TODO: 2019-06-18 Get the codes from the ZenDesk api
    @GetMapping("priority")
    public ResponseEntity getTicketPriority() {
        return ResponseEntity.ok(
                Arrays.asList(
                        TicketPriority.builder().name("Lav").value("low").build(),
                        TicketPriority.builder().name("Høy").value("high").build(),
                        TicketPriority.builder().name("Haster").value("urgent").build()
                )
        );
    }

    @GetMapping("queue/count")
    public ResponseEntity<Integer> getQueueSize() {
        return ResponseEntity.ok(ticketQueuingService.getTicketQueue().size());
    }
}
