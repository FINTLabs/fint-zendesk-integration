package no.fint.provisioning;


import lombok.extern.slf4j.Slf4j;
import no.fint.provisioning.model.Container;
import no.fint.zendesk.ZenDeskUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class UserSyncronizingService {

    @Autowired
    private ZenDeskUserService zenDeskUserService;


    @Autowired
    private BlockingQueue<Container> contactBlockingQueue;

    @Scheduled(fixedRateString = "${fint.zendesk.prov.sync.rate:4000}")
    private void synchronize() throws InterruptedException {
        Container contact = contactBlockingQueue.poll(1, TimeUnit.SECONDS);

        if (contact == null) return;

        if (contact.getAttempts().incrementAndGet() > 10) {
            log.debug("Unable to synchronize contact {} after 10 retries.", contact.getContact().getNin());
            return;
        }

        try {
            if (StringUtils.isEmpty(contact.getContact().getSupportId())) {
                zenDeskUserService.createZenDeskUsers(contact);
            } else {
                zenDeskUserService.updateZenDeskUser(contact);
            }
        } catch (WebClientResponseException e) {
            log.debug("Adding contact back in queue for retry.");
            contactBlockingQueue.add(contact);
        }
        log.debug("{} contacts in queue", contactBlockingQueue.size());
    }


}
