package no.fint.provisioning;

import lombok.extern.slf4j.Slf4j;
import no.fint.ZenDeskProps;
import no.fint.provisioning.model.RequestStatus;
import no.fint.provisioning.model.RequestSynchronizationObject;
import no.fint.provisioning.model.TicketStatus;
import no.fint.provisioning.model.TicketSynchronizationObject;
import no.fint.zendesk.model.ticket.Ticket;
import no.fint.zendesk.model.ticket.vigo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.net.URI;

@Slf4j
@RestController
@CrossOrigin
@RequestMapping("tickets")
public class TicketController {

    @Autowired
    private TicketQueuingService ticketQueuingService;

    @Autowired
    private StatusCache statusCache;

    @Autowired
    private RequestStatusCache requestStatusCache;

    @Autowired
    private ZenDeskProps zenDeskProps;

    @Autowired
    private RequestQueuingService requestQueuingService;

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

    @PostMapping("/vigo-ticket")
    public ResponseEntity createVigoTicket(@RequestBody VigoTicket ticket, HttpServletRequest request) {
        RequestSynchronizationObject requestSynchronizationObject = new RequestSynchronizationObject(ticket);
        requestQueuingService.put(requestSynchronizationObject);
        Ticket emptyTicket = new Ticket();

        RequestStatus requestStatus = RequestStatus.builder().status(RequestStatus.Status.RUNNING).request(emptyTicket).build();
        requestStatusCache.put(requestSynchronizationObject.getUuid(), requestStatus);

        URI location = UriComponentsBuilder.fromUriString(request.getRequestURL().toString().replace("tickets/vigo-ticket", "tickets/"))
                .path("/request-status/{requestid}")
                .buildAndExpand(requestSynchronizationObject.getUuid())
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

    @GetMapping("/request-status/{requestid}")
    public ResponseEntity getRequestStatus(@PathVariable String requestid) {

        log.debug("/request-status/{}", requestid);

        if (!requestStatusCache.containsKey(requestid)) {
            return ResponseEntity.notFound().build();
        }

        RequestStatus requestStatus = requestStatusCache.get(requestid);

        if (requestStatus.getStatus() == RequestStatus.Status.RUNNING) {
            return ResponseEntity.notFound().build();
        }

        if (requestStatus.getStatus() == RequestStatus.Status.ERROR) {
            return ResponseEntity.unprocessableEntity().build();
        }

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(requestStatus.getRequest());
    }

    // TODO: 2019-06-18 Get the codes from the ZenDesk api
    @GetMapping("type")
    public ResponseEntity getTicketTypes() {
        return ResponseEntity.ok(zenDeskProps.getTicketTypes());
    }

    // TODO: 2019-06-18 Get the codes from the ZenDesk api
    @GetMapping("priority")
    public ResponseEntity getTicketPriority() {
        return ResponseEntity.ok(zenDeskProps.getTicketPriorities());
    }

    // TODO: 2019-06-18 Get the codes from the ZenDesk api
    @GetMapping("category")
    public ResponseEntity getTicketCategory() {
        return ResponseEntity.ok(zenDeskProps.getTicketCategory());
    }

    @GetMapping("queue/count")
    public ResponseEntity<Integer> getQueueSize() {
        return ResponseEntity.ok(ticketQueuingService.getTicketQueue().size());
    }
}
