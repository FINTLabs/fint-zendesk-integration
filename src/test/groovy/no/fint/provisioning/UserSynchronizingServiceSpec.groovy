package no.fint.provisioning

import no.fint.ApplicationConfiguration
import no.fint.portal.model.contact.Contact
import no.fint.portal.model.contact.ContactService
import no.fint.provisioning.model.UserSynchronizationObject
import no.fint.zendesk.RateLimiter
import no.fint.zendesk.ZenDeskUserService
import no.fint.zendesk.model.user.User
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import spock.lang.Specification

import java.util.concurrent.BlockingQueue
import java.util.concurrent.TimeUnit

class UserSynchronizingServiceSpec extends Specification {

    private def zenDeskUserService = Mock(ZenDeskUserService)
    private def configuration = new ApplicationConfiguration(ticketSyncMaxRetryAttempts: 10, userSyncMaxRetryAttempts: 10)
    private def userSynchronizeQueue = Mock(BlockingQueue)
    private def contactService = Mock(ContactService)
    private def rateLimiter = Mock(RateLimiter) {
        _ * getRemaining() >>> [2,0]
    }
    private def userSynchronizingService = new UserSynchronizingService(
            zenDeskUserService: zenDeskUserService,
            contactService: contactService,
            configuration: configuration,
            userSynchronizeQueue: userSynchronizeQueue,
            rateLimiter: rateLimiter
    )

    def "When contact has zendesk user update is performed"() {
        when:
        userSynchronizingService.synchronize()

        then:
        1 * userSynchronizeQueue.poll(_ as Long, _ as TimeUnit) >>
                new UserSynchronizationObject(new Contact(supportId: "123"))
        1 * zenDeskUserService.createOrUpdateZenDeskUser(_ as Contact) >> Mono.just(new User(id: 123))
        1 * contactService.updateContact(_ as Contact) >> true
    }

    def "When contact don't have zendesk user create is preformed"() {
        when:
        userSynchronizingService.synchronize()

        then:
        userSynchronizeQueue.poll(_ as Long, _ as TimeUnit) >>
                new UserSynchronizationObject(new Contact())
        1 * zenDeskUserService.createOrUpdateZenDeskUser(_ as Contact) >> Mono.just(new User(id: 123))
        1 * contactService.updateContact(_ as Contact) >> true
    }

    def "If max retries is excised nothing is done"() {
        given:
        def userSynchronizationObject = new UserSynchronizationObject(new Contact(supportId: "123"))
        userSynchronizationObject.attempts.addAndGet(10)

        when:
        userSynchronizingService.synchronize()

        then:
        userSynchronizeQueue.poll(_ as Long, _ as TimeUnit) >> userSynchronizationObject
        1 * zenDeskUserService.createOrUpdateZenDeskUser(_ as Contact) >> Mono.error(new IllegalArgumentException())
        0 * userSynchronizeQueue.offer(_ as UserSynchronizationObject)
    }

    def "When unable to update/create put object back in queue"() {
        when:
        userSynchronizingService.synchronize()

        then:
        userSynchronizeQueue.poll(_ as Long, _ as TimeUnit) >>
                new UserSynchronizationObject(new Contact())
        1 * zenDeskUserService.createOrUpdateZenDeskUser(_ as Contact) >> Mono.error(WebClientResponseException.create(HttpStatus.TOO_MANY_REQUESTS.value(), null, null, null, null))
        1 * userSynchronizeQueue.offer(_ as UserSynchronizationObject) >> true
    }

    def "When the sync queue is empty nothing happens"() {
        when:
        userSynchronizingService.synchronize()

        then:
        userSynchronizeQueue.poll(_ as Long, _ as TimeUnit) >> null
        0 * zenDeskUserService.createOrUpdateZenDeskUser(_ as Contact)
        0 * userSynchronizeQueue.put(_ as UserSynchronizationObject)
    }
}
