package no.fint.provisioning;

import lombok.extern.slf4j.Slf4j;
import no.fint.portal.model.contact.Contact;
import no.fint.provisioning.model.UserSynchronizationObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@RestController
@CrossOrigin
@RequestMapping("users")
@Slf4j
public class UsersController {

    @Autowired
    private BlockingQueue<UserSynchronizationObject> userSynchronizeQueue;

    @PostMapping
    public ResponseEntity<Void> updateContact(@RequestBody Contact contact) {
        try {
            if (userSynchronizeQueue.offer(new UserSynchronizationObject(contact, UserSynchronizationObject.Operation.UPDATE), 1, TimeUnit.SECONDS)) {
                log.info("Updating Contact {}", contact);
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.unprocessableEntity().build();
        } catch (InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteContact(@RequestBody Contact contact) {
        try {
            if (userSynchronizeQueue.offer(new UserSynchronizationObject(contact, UserSynchronizationObject.Operation.DELETE), 1, TimeUnit.SECONDS)) {
                log.info("Deleting Contact {}", contact);
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.unprocessableEntity().build();
        } catch (InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
