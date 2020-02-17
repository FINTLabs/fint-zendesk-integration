package no.fint.provisioning.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import no.fint.portal.model.contact.Contact;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

@Data
@RequiredArgsConstructor
public class UserSynchronizationObject implements Serializable {

    private final Contact contact;
    private final Operation operation;
    private final AtomicInteger attempts = new AtomicInteger();

    public enum Operation { UPDATE, DELETE }
}
