package no.fint.provisioning;

import lombok.extern.slf4j.Slf4j;
import no.fint.zendesk.model.ticket.Ticket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("tickets")
public class TicketController {

    @Autowired
    private TicketQueuingService ticketQueuingService;

    @PostMapping()
    public ResponseEntity createTicket(@RequestBody @Valid Ticket ticket) {
        ticketQueuingService.put(ticket);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("queue/count")
    public ResponseEntity<Integer> getQueueSize()  {
        return ResponseEntity.ok(ticketQueuingService.getTicketQueue().size());
    }
}
