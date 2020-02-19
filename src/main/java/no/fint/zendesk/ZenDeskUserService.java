package no.fint.zendesk;

import lombok.extern.slf4j.Slf4j;
import no.fint.portal.model.contact.Contact;
import no.fint.portal.model.organisation.Organisation;
import no.fint.portal.model.organisation.OrganisationService;
import no.fint.zendesk.model.user.User;
import no.fint.zendesk.model.user.UserRequest;
import no.fint.zendesk.model.user.UserResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Optional;

@Slf4j
@Service
public class ZenDeskUserService {

    @Autowired
    private OrganisationService organisationService;

    @Autowired
    private WebClient webClient;

    public UserResponse createOrUpdateZenDeskUser(Contact contact) {
        log.debug("Updating contact {}", contact.getNin());

        return webClient.post()
                .uri("users/create_or_update.json")
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

    public void deleteZenDeskUser(Contact contact) {
        log.debug("Deleting user {}", contact);
        User user = contactToZenDeskUser(contact);
        webClient.delete()
                .uri("users/destroy_many.json", Collections.singletonMap("external_ids", user.getExternalId()))
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorResume(response -> {
                    if (response instanceof WebClientResponseException) {
                        log.info("\t> Body: {}", ((WebClientResponseException) response).getResponseBodyAsString());
                    }
                    return Mono.error(response);
                })
                .block();
    }

    private User contactToZenDeskUser(Contact contact) {
        User user = User.builder()
                .externalId("fint_" + maskNin(contact.getNin()))
                .details(getDetails(contact))
                .email(contact.getMail())
                .name(getFullname(contact))
                .role(getRole(contact.getMail()))
                .verified(true)
                .signature(getSignature(contact))
                .notes("Brukeren vedlikeholdes automatisk gjennom kundeportalen.")
                .phone(contact.getMobile()).build();
        log.debug("User: {}", user);
        return user;
    }

    private String getRole(String mail) {
        if (StringUtils.endsWithAny(mail, "@fintlabs.no", "@vigodrift.no")) {
            return "admin";
        }
        return "end-user";
    }

    private String maskNin(String nin) {
        return Long.toString((Long.parseLong(nin) / 100), 36);
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
