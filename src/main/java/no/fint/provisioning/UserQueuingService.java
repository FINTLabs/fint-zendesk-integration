package no.fint.provisioning;

import lombok.extern.slf4j.Slf4j;
import no.fint.cache.FintCache;
import no.fint.cache.model.CacheObject;
import no.fint.portal.model.contact.ContactService;
import no.fint.provisioning.model.Container;
import no.fint.zendesk.ZenDeskUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserQueuingService {

    @Autowired
    private ContactService contactService;

    @Autowired
    private ZenDeskUserService zenDeskUserService;

    @Autowired
    private BlockingQueue<Container> userSynchronizeQueue;

    @Autowired
    private BlockingQueue<String> userDeleteQueue;

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
        contactCache.getSince(lastUpdated).forEach(this::putOnSynchronizeQueue);
        lastUpdated = contactCache.getLastUpdated();

        zenDeskUserService.getOrphantUsers().forEach(this::putOnDeleteQueue);

    }


    private void putOnSynchronizeQueue(CacheObject<Container> contactCacheObject) {
        try {
            userSynchronizeQueue.put(contactCacheObject.getObject());
            log.debug("New contact added to synchronize queue");
            log.debug("{} contacts in synchronize queue", userSynchronizeQueue.size());
        } catch (InterruptedException e) {
            log.error("Unable to put contact to synchronize queue: {}", e.getMessage());
        }
    }

    private void putOnDeleteQueue(String id) {
        try {
            userDeleteQueue.put(id);
            log.debug("New contact added to delete queue");
            log.debug("{} contacts in delete queue", userDeleteQueue.size());
        } catch (InterruptedException e) {
            log.error("Unable to put contact to delete queue: {}", e.getMessage());
        }
    }
}
