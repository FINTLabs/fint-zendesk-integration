package no.fint.provisioning

import no.fint.ApplicationConfiguration
import no.fint.cache.FintCache
import no.fint.portal.model.contact.Contact
import no.fint.portal.model.contact.ContactService
import no.fint.provisioning.model.UserSynchronizationObject
import no.fint.zendesk.ZenDeskUserService
import spock.lang.Ignore
import spock.lang.Specification

import java.util.stream.Collectors

class UserQueuingServiceSpec extends Specification {

    private def zenDeskUserService = Mock(ZenDeskUserService)
    private def conftactService = Mock(ContactService)
    private def config = new ApplicationConfiguration()
    private def userQueuingService = new UserQueuingService(
            contactService: conftactService,
            userDeleteQueue: config.userDeleteQueue(),
            userSynchronizeQueue: config.userSynchronizeQueue(),
            zenDeskUserService: zenDeskUserService,
            contactCache: new FintCache<UserSynchronizationObject>(),
            lastUpdated: 0
    )

    @Ignore
    def "The sync queue size is equal to updated objects in cache"() {

        when:
        userQueuingService.queue()
        def size = userQueuingService.contactCache.getSince(0).collect(Collectors.toList()).size()


        then:
        1 * zenDeskUserService.orphantUsers >> Arrays.asList()
        conftactService.getContacts() >> Arrays.asList(new Contact(nin: "1"))
        userQueuingService.userSynchronizeQueue.size() == size
    }

    @Ignore
    def "The delete queue is equal to size of orphantUsers"() {
        when:
        userQueuingService.queue()
        def size = zenDeskUserService.orphantUsers.size()

        then:
        2 * zenDeskUserService.orphantUsers >> Arrays.asList("1")
        1 * conftactService.getContacts() >> Arrays.asList()
        userQueuingService.userDeleteQueue.size() == size

    }
}
