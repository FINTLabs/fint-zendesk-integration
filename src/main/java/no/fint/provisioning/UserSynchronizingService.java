package no.fint.provisioning;


import lombok.extern.slf4j.Slf4j;
import no.fint.ApplicationConfiguration;
import no.fint.portal.model.contact.Contact;
import no.fint.provisioning.model.UserSynchronizationObject;
import no.fint.zendesk.RateLimiter;
import no.fint.zendesk.ZenDeskUserService;
import no.fint.zendesk.model.user.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class UserSynchronizingService {

    @Autowired
    private ZenDeskUserService zenDeskUserService;

    @Autowired
    private ApplicationConfiguration configuration;

    @Autowired
    private BlockingQueue<UserSynchronizationObject> userSynchronizeQueue;

    @Autowired
    private RateLimiter rateLimiter;

    @Scheduled(fixedDelayString = "${fint.zendesk.user.sync.rate:60000}")
    private void synchronize() throws InterruptedException {
        log.info("Starting user sync with {} pending updates...", userSynchronizeQueue.size());
        do {
            UserSynchronizationObject update = userSynchronizeQueue.poll(10, TimeUnit.SECONDS);

            if (update == null) break;
            Contact contact = update.getContact();

            if (update.getAttempts().incrementAndGet() > configuration.getUserSyncMaxRetryAttempts()) {
                log.debug("Unable to synchronize contact {} after 10 retries.", contact.getNin());
                continue;
            }

            try {
                if (update.getOperation() == UserSynchronizationObject.Operation.DELETE) {
                    zenDeskUserService.deleteZenDeskUser(contact);
                } else {
                    UserResponse userResponse = zenDeskUserService.createOrUpdateZenDeskUser(contact);
                    log.info("User ID: {}", userResponse.getUser().getId());
                }
                log.info("Remaining: {}", rateLimiter.getRemaining());
                TimeUnit.SECONDS.sleep(1);
            } catch (WebClientResponseException e) {
                log.debug("Adding contact back in queue for retry.", e);
                userSynchronizeQueue.put(update);
                break;
            }
        } while (rateLimiter.getRemaining() > 1);
        log.info("Pending contacts: {}", userSynchronizeQueue.size());
    }


}
