package no.fint.provisioning;


import lombok.extern.slf4j.Slf4j;
import no.fint.ApplicationConfiguration;
import no.fint.portal.model.contact.Contact;
import no.fint.portal.model.contact.ContactService;
import no.fint.provisioning.model.UserSynchronizationObject;
import no.fint.zendesk.RateLimiter;
import no.fint.zendesk.ZenDeskUserService;
import no.fint.zendesk.model.user.User;
import org.apache.commons.lang3.StringUtils;
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
        do {
            UserSynchronizationObject update = userSynchronizeQueue.poll(10, TimeUnit.SECONDS);

            if (update == null) break;
            Contact contact = update.getContact();
            final String name = contact.getFirstName() + " " + contact.getLastName();

            try {
                // Only create users for contacts who are related to an organisation.
                if (contact.getLegal().isEmpty() && contact.getTechnical().isEmpty()) {
                    if (StringUtils.isNumeric(contact.getSupportId())) {
                        log.info("Deleting user {}", name);
                        final User response = zenDeskUserService
                                .deleteZenDeskUser(contact.getSupportId())
                                .block(timeout);
                        log.debug("Remaining: {}", rateLimiter.getRemaining());
                        log.debug("User ID: {}", response.getId());
                        contact.setSupportId(null);
                        contactService.updateContact(contact);
                    }
                } else {
                    log.info("Updating user {}", name);
                    User response = zenDeskUserService
                            .createOrUpdateZenDeskUser(contact)
                            .block(timeout);
                    log.debug("Remaining: {}", rateLimiter.getRemaining());
                    log.debug("User ID: {}", response.getId());
                    contact.setSupportId(String.valueOf(response.getId()));
                    contactService.updateContact(contact);
                }
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {
                if (update.getAttempts().incrementAndGet() >= configuration.getUserSyncMaxRetryAttempts()) {
                    log.warn("Unable to synchronize contact {} after 10 retries.", name);
                } else {
                    log.debug("Adding contact {} back in queue for retry.", name, e);
                    userSynchronizeQueue.offer(update);
                }
                break;
            }
        } while (rateLimiter.getRemaining() > 1);
        log.info("Pending contacts: {}", userSynchronizeQueue.size());
    }

}
