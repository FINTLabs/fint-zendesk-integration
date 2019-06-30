package no.fint.provisioning;

import lombok.extern.slf4j.Slf4j;
import no.fint.zendesk.model.ticket.Ticket;
import no.fint.zendesk.model.ticket.TicketPriority;
import no.fint.zendesk.model.ticket.TicketType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Arrays;

@Slf4j
@RestController
@CrossOrigin
@RequestMapping("tickets")
public class TicketController {

    @Autowired
    private TicketQueuingService ticketQueuingService;

    @PostMapping
    public ResponseEntity createTicket(@RequestBody @Valid Ticket ticket) {
        ticketQueuingService.put(ticket);
        return ResponseEntity.status(HttpStatus.OK).build();
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
