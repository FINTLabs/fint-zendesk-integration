package no.fint.provisioning;


import lombok.extern.slf4j.Slf4j;
import no.fint.ApplicationConfiguration;
import no.fint.portal.model.contact.Contact;
import no.fint.portal.model.contact.ContactService;
import no.fint.provisioning.model.UserSynchronizationObject;
import no.fint.zendesk.RateLimiter;
import no.fint.zendesk.ZenDeskUserService;
import no.fint.zendesk.model.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class UserSynchronizingService {

    @Value("${fint.zendesk.timeout:PT30S}")
    private Duration timeout;

    @Autowired
    private ZenDeskUserService zenDeskUserService;

    @Autowired
    private ApplicationConfiguration configuration;

    @Autowired
    private BlockingQueue<UserSynchronizationObject> userSynchronizeQueue;

    @Autowired
    private RateLimiter rateLimiter;

    @Autowired
    private ContactService contactService;

    @Scheduled(fixedDelayString = "${fint.zendesk.user.sync.rate:60000}")
    private void synchronize() throws InterruptedException {
        log.info("Starting user sync with {} pending updates...", userSynchronizeQueue.size());
        while (rateLimiter.getRemaining() > 1) {
            UserSynchronizationObject update = userSynchronizeQueue.poll(10, TimeUnit.SECONDS);

            if (update == null) break;
            Contact contact = update.getContact();


            try {
                User response = zenDeskUserService
                        .createOrUpdateZenDeskUser(update.getContact())
                        .block(timeout);
                log.info("Remaining: {}", rateLimiter.getRemaining());
                log.info("User ID: {}", response.getId());
                contact.setSupportId(String.valueOf(response.getId()));
                contactService.updateContact(contact);
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {
                if (update.getAttempts().incrementAndGet() >= configuration.getUserSyncMaxRetryAttempts()) {
                    log.debug("Unable to synchronize contact {} after 10 retries.", contact.getNin());
                } else {
                    log.debug("Adding contact back in queue for retry.", e);
                    userSynchronizeQueue.offer(update);
                }
                break;
            }
        }
        log.info("Pending contacts: {}", userSynchronizeQueue.size());
    }

}
