package no.fint.provisioning

import no.fint.ApplicationConfiguration
import no.fint.provisioning.model.TicketSynchronizationObject
import no.fint.zendesk.RateLimiter
import no.fint.zendesk.ZenDeskTicketService
import no.fint.zendesk.model.ticket.Ticket
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClientResponseException
import spock.lang.Specification

import java.util.concurrent.BlockingQueue
import java.util.concurrent.TimeUnit

class TicketSynchronizingServiceSpec extends Specification {

    private def ticketQueue = Mock(BlockingQueue)
    private def configuration = new ApplicationConfiguration(ticketSyncMaxRetryAttempts: 10)
    private def zenDeskTicketService = Mock(ZenDeskTicketService)
    private def rateLimiter = Mock(RateLimiter) {
        _ * getRemaining() >> 0
    }
    private def ticketSynchronizingService = new TicketSynchronizingService(
            ticketQueue: ticketQueue,
            configuration: configuration,
            zenDeskTicketService: zenDeskTicketService,
            statusCache: Mock(StatusCache),
            rateLimiter: rateLimiter
    )

    def "When the sync queue is empty nothing happens"() {

        when:
        ticketSynchronizingService.synchronize()

        then:
        ticketQueue.poll(_ as Long, _ as TimeUnit) >> null
        0 * zenDeskTicketService.createTicket(_ as TicketSynchronizationObject)
        0 * ticketQueue.put(_ as TicketSynchronizationObject)
    }

    def "When there's a ticket in the queue it is created"() {
        when:
        ticketSynchronizingService.synchronize()

        then:
        ticketQueue.poll(_ as Long, _ as TimeUnit) >> new TicketSynchronizationObject(new Ticket())
        1 * zenDeskTicketService.createTicket(_ as TicketSynchronizationObject)
    }

    def "If max retries is excised nothing is done"() {
        given:
        def ticketSynchronizationObject = new TicketSynchronizationObject(new Ticket())
        ticketSynchronizationObject.attempts.addAndGet(10)

        when:
        ticketSynchronizingService.synchronize()

        then:
        ticketQueue.poll(_ as Long, _ as TimeUnit) >> ticketSynchronizationObject
        0 * zenDeskTicketService.createTicket(_ as TicketSynchronizationObject)
        0 * ticketQueue.put(_ as TicketSynchronizationObject)
    }

    def "When unable to create put object back in queue"() {

        when:
        ticketSynchronizingService.synchronize()

        then:
        ticketQueue.poll(_ as Long, _ as TimeUnit) >> new TicketSynchronizationObject(new Ticket())
        zenDeskTicketService.createTicket(_ as TicketSynchronizationObject) >> {
            throw WebClientResponseException.create(
                    HttpStatus.TOO_MANY_REQUESTS.value(),
                    null,
                    null,
                    null,
                    null)
        }
        1 * ticketQueue.put(_ as TicketSynchronizationObject)

    }
}
