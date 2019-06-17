package no.fint.provisioning;

import lombok.extern.slf4j.Slf4j;
import no.fint.cache.FintCache;
import no.fint.cache.model.CacheObject;
import no.fint.portal.model.contact.ContactService;
import no.fint.provisioning.model.Container;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserQueuingService {

    @Autowired
    private ContactService contactService;

    @Autowired
    private BlockingQueue<Container> contactBlockingQueue;

    private FintCache<Container> contactCache;
    private long lastUpdated;



    @PostConstruct
    private void init() {
        contactCache = new FintCache<>();
        lastUpdated = 0;
    }

    @Scheduled(initialDelayString = "${fint.zendesk.prov.queuing.initial-delay:10000}", fixedDelayString = "${fint.zendesk.prov.queuing.delay:60000}")
    public void queue() {
        contactCache.update(contactService.getContacts()
                .stream()
                .map(Container::new)
                .collect(Collectors.toList()));
        log.info("{} contacts needs to be queued for synchronization.", contactCache.getSince(lastUpdated).count());
        contactCache.getSince(lastUpdated).forEach(this::putOnQueue);
        lastUpdated = contactCache.getLastUpdated();
    }


    private void putOnQueue(CacheObject<Container> contactCacheObject) {
        try {
            contactBlockingQueue.put(contactCacheObject.getObject());
            log.debug("New contact added to queue");
            log.debug("{} contacts in queue", contactBlockingQueue.size());
        } catch (InterruptedException e) {
            log.error("Unable to put contact to queue: {}", e.getMessage());
        }
    }
}
