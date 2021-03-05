package no.fint.provisioning

import no.fint.ApplicationConfiguration
import no.fint.provisioning.model.TicketSynchronizationObject
import spock.lang.Specification

class TicketQueuingServiceSpec extends Specification {


    private def config = new ApplicationConfiguration()
    private def ticketQueuingService = new TicketQueuingService(ticketQueue: config.ticketQueue(0))

    def "When adding a object to queue the queue size increases by one"() {

        when:
        ticketQueuingService.put(new TicketSynchronizationObject())

        then:
        ticketQueuingService.ticketQueue.size() == 1

    }
}
