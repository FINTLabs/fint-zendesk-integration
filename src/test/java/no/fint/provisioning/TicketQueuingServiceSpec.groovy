package no.fint.provisioning

import no.fint.ApplicationConfiguration
import no.fint.zendesk.model.ticket.Ticket
import spock.lang.Specification

class TicketQueuingServiceSpec extends Specification {


    private def config = new ApplicationConfiguration()
    private def ticketQueuingService = new TicketQueuingService(ticketQueue: config.ticketQueue())

    def "When adding a object to queue the queue size increases by one"() {

        when:
        ticketQueuingService.put(new Ticket())

        then:
        ticketQueuingService.ticketQueue.size() == 1

    }
}
