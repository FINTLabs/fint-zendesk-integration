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
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

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
    public ResponseEntity createTicket(@RequestBody @Valid Ticket ticket) {
        TicketSynchronizationObject ticketSynchronizationObject = new TicketSynchronizationObject(ticket);
        if (!ticketQueuingService.put(ticketSynchronizationObject)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).header("x-error-message", "Unable to queue ticket for processing").build();
        }

        TicketStatus ticketStatus = TicketStatus.builder().status(TicketStatus.Status.RUNNING).ticket(ticket).build();
        statusCache.put(ticketSynchronizationObject.getUuid(), ticketStatus);

        URI location = MvcUriComponentsBuilder.fromMethodCall(
                MvcUriComponentsBuilder
                        .controller(TicketController.class)
                        .getStatus(ticketSynchronizationObject.getUuid()))
                .build()
                .toUri();
        log.debug("Location: {}", location);
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
                        TicketType.builder()
                                .name("Spørsmål")
                                .value("question")
                                .help("Velg denne om du lurer på noe eller trenger råd. Dette er det samme som Service Request i ITIL.")
                                .build(),
                        TicketType.builder()
                                .name("Hendelse")
                                .value("incident")
                                .help("Velg denne om det er en ikke planlagt forstyrrelse/feil på en tjeneste. Dette er det samme som Incident i ITIL.")
                                .build()
                )
        );
    }

    // TODO: 2019-06-18 Get the codes from the ZenDesk api
    @GetMapping("priority")
    public ResponseEntity getTicketPriority() {
        return ResponseEntity.ok(
                Arrays.asList(
                        TicketPriority.builder()
                                .name("Lav")
                                .value("low")
                                .help("Velg prioritet ut i fra følgende kriterier:" +
                                        "<ul>" +
                                        "<li>Mindre feil eller \"kosmetiske\" problemer.</li>" +
                                        "<li>Feil påvirker arbeidet, men sluttbruker kan fortsette sitt arbeid med noe redusert ytelse.</li>" +
                                        "<li>Liten eller ingen økonomisk skadevirkning</li>" +
                                        "</ul>" +
                                        "<p>Responstid innen 5 virkedager.</p>")
                                .build(),
                        TicketPriority.builder()
                                .name("Normal")
                                .value("normal")
                                .help("Velg prioritet ut i fra følgende kriterier:" +
                                        "<ul>" +
                                        "<li>En sluttbruker får ikke utført sine arbeidsfunksjoner.</li>" +
                                        "<li>Liten økonomisk skadevirkning.</li>" +
                                        "</ul>" +
                                        "<p>Responstid innen 10 timer.</p>")
                                .build(),
                        TicketPriority.builder()
                                .name("Høy")
                                .value("high")
                                .help("Velg prioritet ut i fra følgende kriterier:" +
                                        "<ul>" +
                                        "<li>Flere brukere får ikke utført sine arbeidsfunksjoner.</li>" +
                                        "<li>En viss økonomisk skadevirkning.</li>" +
                                        "</ul>" +
                                        "NB! Det forventes at dere er tilgjengelige frem til saken er løst og har mulighet" +
                                        " til å bistå oss med hjelp og informasjon."+
                                        "<p>Responstid innen 4 timer.</p>")
                                .build(),
                        TicketPriority.builder()
                                .name("Haster")
                                .value("urgent")
                                .help("Velg prioritet ut i fra følgende kriterier:" +
                                        "<ul>" +
                                        "<li>Utfall av en eller flere lokasjoner/tjenester.</li>" +
                                        "<li>Ingen brukere får utført sine arbeidsfunksjoner.</li>" +
                                        "<li>Store økonomiske tap.</li>" +
                                        "<li>Alvorlig konsekvens for omdømme.</li>" +
                                        "</ul>" +
                                        "NB! Det forventes at dere er tilgjengelige frem til saken er løst og har mulighet" +
                                        " til å bistå oss med hjelp og informasjon." +
                                        "<p>Responstid innen 60 minutter.</p>")
                                .build()
                )
        );
    }

    @GetMapping("queue/count")
    public ResponseEntity<Integer> getQueueSize() {
        return ResponseEntity.ok(ticketQueuingService.getTicketQueue().size());
    }
}
