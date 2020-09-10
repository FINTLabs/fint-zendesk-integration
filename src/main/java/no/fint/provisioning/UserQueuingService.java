package no.fint.provisioning;

import lombok.extern.slf4j.Slf4j;
import no.fint.cache.FintCache;
import no.fint.cache.model.CacheObject;
import no.fint.portal.model.contact.ContactService;
import no.fint.provisioning.model.UserSynchronizationObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

@Slf4j
@Service
@ConditionalOnProperty("fint.zendesk.users.enabled")
public class UserQueuingService {

    @Autowired
    private ContactService contactService;

    @Autowired
    private BlockingQueue<UserSynchronizationObject> userSynchronizeQueue;

    private FintCache<UserSynchronizationObject> contactCache;
    private long lastUpdated;


    @PostConstruct
    private void init() {
        contactCache = new FintCache<>();
        lastUpdated = 0;
        log.info("FINT Zendesk user service enabled.");
    }

    @Scheduled(initialDelayString = "${fint.zendesk.prov.user.queuing.initial-delay:10000}", fixedDelayString = "${fint.zendesk.prov.user.queuing.delay:60000}")
    public void queue() {
        contactCache.update(contactService.getContacts()
                .stream()
                .map(c -> new UserSynchronizationObject(c, UserSynchronizationObject.Operation.UPDATE))
                .collect(Collectors.toList()));
        long count = contactCache.getSince(lastUpdated).peek(this::putOnSynchronizeQueue).count();
        log.info("{} contacts queued for synchronization, new queue size {}", count, userSynchronizeQueue.size());
        lastUpdated = contactCache.getLastUpdated();
    }


    private void putOnSynchronizeQueue(CacheObject<UserSynchronizationObject> contactCacheObject) {
        try {
            userSynchronizeQueue.put(contactCacheObject.getObject());
        } catch (InterruptedException e) {
            log.error("Unable to put contact to synchronize queue: {}", e.getMessage());
        }
    }
}
