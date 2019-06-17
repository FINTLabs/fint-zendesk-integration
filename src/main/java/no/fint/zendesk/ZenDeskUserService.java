package no.fint.zendesk;

import lombok.extern.slf4j.Slf4j;
import no.fint.portal.model.contact.Contact;
import no.fint.portal.model.contact.ContactService;
import no.fint.portal.model.organisation.Organisation;
import no.fint.portal.model.organisation.OrganisationService;
import no.fint.provisioning.model.Container;
import no.fint.zendesk.model.user.ZenDeskUser;
import no.fint.zendesk.model.user.ZenDeskUserRequest;
import no.fint.zendesk.model.user.ZenDeskUserResponse;
import no.fint.zendesk.model.user.ZenDeskUsersResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ZenDeskUserService {

    @Autowired
    private OrganisationService organisationService;

    @Autowired
    private ContactService contactService;

    @Autowired
    private WebClient webClient;

    public void createZenDeskUsers(Container container) {
        Contact contact = container.getContact();
        log.debug("Creating contact {}", contact.getNin());
        log.debug("Attempt: {}", container.getAttempts());

        webClient.post()
                .uri("users")
                .syncBody(new ZenDeskUserRequest(contactToZenDeskUser(contact)))
                .retrieve()
                .bodyToMono(ZenDeskUserResponse.class)
                .doOnSuccess(response -> {
                    contact.setSupportId(Long.toString(response.getUser().getId()));
                    contactService.updateContact(contact);
                })
                .onErrorResume(response -> {
                    if (response instanceof WebClientResponseException) {
                        log.info("\t> Body: {}", ((WebClientResponseException) response).getResponseBodyAsString());
                    }
                    return Mono.error(response);
                })
                .block();

    }

    public void updateZenDeskUser(Container container) {
        Contact contact = container.getContact();
        log.debug("Updating contact {}", contact.getNin());
        log.debug("Attempt: {}", container.getAttempts());

        webClient.put()
                .uri(String.format("users/%s.json", contact.getSupportId()))
                .syncBody(new ZenDeskUserRequest(contactToZenDeskUser(contact)))
                .retrieve()
                .bodyToMono(ZenDeskUserResponse.class)
                .onErrorResume(response -> {
                    if (response instanceof WebClientResponseException) {
                        log.info("\t> Body: {}", ((WebClientResponseException) response).getResponseBodyAsString());
                    }
                    return Mono.error(response);
                })
                .block();
    }

    public void deleteZenDeskUser(String id) {
        log.debug("Deleting user {}", id);

        webClient.delete()
                .uri(String.format("users/%s.json", id))
                .retrieve()
                .bodyToMono(ZenDeskUserResponse.class)
                .onErrorResume(response -> {
                    if (response instanceof WebClientResponseException) {
                        log.info("\t> Body: {}", ((WebClientResponseException) response).getResponseBodyAsString());
                    }
                    return Mono.error(response);
                })
                .block();
    }

    public List<String> getOrphantUsers() {
        List<String> contacts = contactService.getContacts().stream().map(Contact::getSupportId).collect(Collectors.toList());
        List<String> zenDeskUsers = getZenDeskUsers().stream().map(z -> Long.toString(z.getId())).collect(Collectors.toList());
        zenDeskUsers.removeAll(contacts);
        return zenDeskUsers;
    }

    private List<ZenDeskUser> getZenDeskUsers() {
        ZenDeskUsersResponse zenDeskUsersResponse = webClient.get()
                .uri("users.json")
                .retrieve()
                .bodyToMono(ZenDeskUsersResponse.class)
                .onErrorResume(response -> {
                    if (response instanceof WebClientResponseException) {
                        log.info("\t> Body: {}", ((WebClientResponseException) response).getResponseBodyAsString());
                    }
                    return Mono.error(response);
                })
                .block();
        return zenDeskUsersResponse.getUsers();
    }

    private ZenDeskUser contactToZenDeskUser(Contact contact) {
        return ZenDeskUser.builder()
                .details(getDetails(contact))
                .email(contact.getMail())
                .name(String.format("%s %s", contact.getFirstName(), contact.getLastName()))
                .phone(contact.getMobile()).build();
    }

    private String getDetails(Contact contact) {
        StringBuilder stringBuilder = new StringBuilder();

        if (contact.getLegal().size() > 0) {
            stringBuilder.append("Juridisk kontaktperson for:\n\n");
            contact.getLegal().forEach(dn -> {
                Optional<Organisation> organisation = organisationService.getOrganisationByDn(dn);
                organisation.ifPresent(o -> stringBuilder.append("- ").append(o.getDisplayName()).append("\n"));
            });
            stringBuilder.append("\n\n");
        }

        if (contact.getTechnical().size() > 0) {
            stringBuilder.append("Teknisk kontaktperson for:\n\n");
            contact.getTechnical().forEach(dn -> {
                Optional<Organisation> organisation = organisationService.getOrganisationByDn(dn);
                organisation.ifPresent(o -> stringBuilder.append("- ").append(o.getDisplayName()).append("\n"));
            });
        }

        return stringBuilder.toString();
    }
}
