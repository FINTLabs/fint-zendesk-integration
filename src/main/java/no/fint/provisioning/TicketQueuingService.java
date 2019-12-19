package no.fint.provisioning;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import no.fint.provisioning.model.TicketSynchronizationObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;

@Slf4j
@Getter
@Service
public class TicketQueuingService {

    @Autowired
    private BlockingQueue<TicketSynchronizationObject> ticketQueue;

    public void put(TicketSynchronizationObject ticket) {
        try {
            ticketQueue.put(ticket);
        } catch (InterruptedException e) {
            log.error("Unable to put ticket on queue", e);
        }
    }
}
