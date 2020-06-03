package no.fint.zendesk;

import lombok.extern.slf4j.Slf4j;
import no.fint.portal.model.contact.Contact;
import no.fint.portal.model.contact.ContactService;
import no.fint.portal.model.organisation.Organisation;
import no.fint.portal.model.organisation.OrganisationService;
import no.fint.provisioning.model.UserSynchronizationObject;
import no.fint.zendesk.model.user.User;
import no.fint.zendesk.model.user.UserRequest;
import no.fint.zendesk.model.user.UserResponse;
import no.fint.zendesk.model.user.UsersResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@ConditionalOnProperty("fint.zendesk.users.enabled")
public class ZenDeskUserService {

    @Autowired
    private OrganisationService organisationService;

    @Autowired
    private ContactService contactService;

    @Autowired
    private WebClient webClient;

    public void createZenDeskUsers(UserSynchronizationObject userSynchronizationObject) {
        Contact contact = userSynchronizationObject.getContact();
        log.debug("Creating contact {}", contact.getNin());
        log.debug("\tAttempt: {}", userSynchronizationObject.getAttempts());

        webClient.post()
                .uri("users")
                .syncBody(new UserRequest(contactToZenDeskUser(contact)))
                .retrieve()
                .bodyToMono(UserResponse.class)
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

    public void updateZenDeskUser(UserSynchronizationObject userSynchronizationObject) {
        Contact contact = userSynchronizationObject.getContact();
        log.debug("Updating contact {}", contact.getNin());
        log.debug("\tAttempt: {}", userSynchronizationObject.getAttempts());

        webClient.put()
                .uri(String.format("users/%s.json", contact.getSupportId()))
                .syncBody(new UserRequest(contactToZenDeskUser(contact)))
                .retrieve()
                .bodyToMono(UserResponse.class)
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
                .bodyToMono(UserResponse.class)
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
        List<String> zenDeskUsers = null;
        try {
            zenDeskUsers = getZenDeskUsers().stream().map(z -> Long.toString(z.getId())).collect(Collectors.toList());
        } catch (WebClientResponseException e) {
            log.debug("Unable to get all ZenDesk users at the moment. Darn license \\xF0\\x9F\\x99\\x8A");
        }
        zenDeskUsers.removeAll(contacts);
        return zenDeskUsers;
    }

    private List<User> getZenDeskUsers() {
        log.debug("Getting all ZenDesk users");
        UsersResponse usersResponse = webClient.get()
                .uri("users.json")
                .retrieve()
                .bodyToMono(UsersResponse.class)
                .onErrorResume(response -> {
                    if (response instanceof WebClientResponseException) {
                        log.info("\t> Body: {}", ((WebClientResponseException) response).getResponseBodyAsString());
                    }
                    return Mono.error(response);
                })
                .block();
        return usersResponse.getUsers();
    }

    private User contactToZenDeskUser(Contact contact) {
        return User.builder()
                .details(getDetails(contact))
                .email(contact.getMail())
                .name(getFullname(contact))
                .verified(true)
                .signature(getSignature(contact))
                .notes("Brukeren vedlikeholdes automatisk gjennom kundeportalen.")
                .phone(contact.getMobile()).build();
    }

    private String getDetails(Contact contact) {
        StringBuilder stringBuilder = new StringBuilder();

        if (contact.getLegal().size() > 0) {
            stringBuilder.append("Juridisk kontaktperson for:\n");
            contact.getLegal().forEach(dn -> {
                Optional<Organisation> organisation = organisationService.getOrganisationByDn(dn);
                organisation.ifPresent(o -> stringBuilder.append("* ").append(o.getDisplayName()).append("\n"));
            });
            stringBuilder.append("\n\n");
        }

        if (contact.getTechnical().size() > 0) {
            stringBuilder.append("Teknisk kontaktperson for:\n");
            contact.getTechnical().forEach(dn -> {
                Optional<Organisation> organisation = organisationService.getOrganisationByDn(dn);
                organisation.ifPresent(o -> stringBuilder.append("* ").append(o.getDisplayName()).append("\n"));
            });
        }

        return stringBuilder.toString();
    }

    private String getSignature(Contact contact) {
        return String.format("Med vennlig hilsen\n%s %s",
                contact.getFirstName(),
                contact.getLastName()
        );
    }

    private String getFullname(Contact contact) {
        return String.format("%s %s", contact.getFirstName(), contact.getLastName());
    }
}
