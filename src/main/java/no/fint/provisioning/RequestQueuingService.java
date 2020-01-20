package no.fint.provisioning;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import no.fint.provisioning.model.RequestSynchronizationObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;

@Slf4j
@Getter
@Service
public class RequestQueuingService {

    @Autowired
    private BlockingQueue<RequestSynchronizationObject> requestQueue;

    public void put(RequestSynchronizationObject ticket) {
        try {
            requestQueue.put(ticket);
        } catch (InterruptedException e) {
            log.error("Unable to put ticket on queue", e);
        }
    }
}
