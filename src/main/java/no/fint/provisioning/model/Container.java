package no.fint.provisioning.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import no.fint.portal.model.contact.Contact;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

@Data
@RequiredArgsConstructor
public class Container implements Serializable {

    private final Contact contact;
    private final AtomicInteger attempts = new AtomicInteger();
}
