package no.fint.provisioning;

import lombok.extern.slf4j.Slf4j;
import no.fint.ApplicationConfiguration;
import no.fint.provisioning.model.RequestStatus;
import no.fint.provisioning.model.RequestSynchronizationObject;
import no.fint.zendesk.ZebDeskRequestService;
import no.fint.zendesk.model.ticket.Ticket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RequestSynchronizingService {

    @Autowired
    private BlockingQueue<RequestSynchronizationObject> requestQueue;

    @Autowired
    private ApplicationConfiguration configuration;

    @Autowired
    private ZebDeskRequestService zebDeskRequestService;

    @Autowired
    private RequestStatusCache requestStatusCache;

    @Scheduled(fixedRateString = "${fint.zendesk.ticket.sync.rate:5000}")
    private void synchronize() throws InterruptedException {
        RequestSynchronizationObject ticket = requestQueue.poll(1, TimeUnit.SECONDS);

        if (ticket == null) return;

        if (ticket.getAttempts().incrementAndGet() > configuration.getTicketSyncMaxRetryAttempts()) {
            log.debug("Unable to synchronize ticket after 10 retries.");
            requestStatusCache.put(ticket.getUuid(), RequestStatus.builder().status(RequestStatus.Status.ERROR).build());
            return;
        }

        try {
            Ticket ticketResponse = zebDeskRequestService.createTicket(ticket);
            requestStatusCache.put(ticket.getUuid(), RequestStatus.builder()
                    .status(RequestStatus.Status.CREATED)
                    .request(ticketResponse)
                    .build()
            );

        } catch (Exception e) {
            log.debug("Adding ticket back in queue for retry.", e);
            requestQueue.put(ticket);
        }

    }

}
