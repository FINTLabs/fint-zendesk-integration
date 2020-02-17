package no.fint.provisioning;


import lombok.extern.slf4j.Slf4j;
import no.fint.ApplicationConfiguration;
import no.fint.provisioning.model.UserSynchronizationObject;
import no.fint.zendesk.ZenDeskUserService;
import org.apache.commons.lang3.StringUtils;
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
    private BlockingQueue<String> userDeleteQueue;

    @Scheduled(fixedRateString = "${fint.zendesk.user.sync.rate:60000}")
    private void synchronize() throws InterruptedException {
        UserSynchronizationObject contact = userSynchronizeQueue.poll(1, TimeUnit.SECONDS);

        if (contact == null) return;

        // TODO This is a workaround for this service dropping privileges to user!
        if (StringUtils.endsWithAny(contact.getContact().getMail(), "@fintlabs.no", "@vigodrift.no")) {
            log.error("Not updating contact with email {} - user might be agent or admin!!", contact.getContact().getMail());
            return;
        }

        if (contact.getAttempts().incrementAndGet() > configuration.getUserSyncMaxRetryAttempts()) {
            log.debug("Unable to synchronize contact {} after 10 retries.", contact.getContact().getNin());
            return;
        }

        try {
            if (contact.getOperation() == UserSynchronizationObject.Operation.DELETE) {
                zenDeskUserService.deleteZenDeskUser(contact);
            } else if (contactHasZenDeskUser(contact)) {
                zenDeskUserService.updateZenDeskUser(contact);
            } else {
                zenDeskUserService.createZenDeskUsers(contact);
            }
        } catch (WebClientResponseException e) {
            log.debug("Adding contact back in queue for retry.", e);
            userSynchronizeQueue.put(contact);
        }
        log.debug("{} contacts in synchronize queue", userSynchronizeQueue.size());
    }

    private boolean contactHasZenDeskUser(UserSynchronizationObject contact) {
        return StringUtils.isNotBlank(contact.getContact().getSupportId());
    }


}
