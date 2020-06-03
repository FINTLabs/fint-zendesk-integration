package no.fint.zendesk


import no.fint.zendesk.model.ticket.Ticket
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import spock.lang.Specification

class ZenDeskTicketServiceSpec extends Specification {

    private def server = new MockWebServer()
    private def zenDeskTicketService = new ZenDeskTicketService(
            webClient: WebClient.create(server.url('/').toString())
    )

    def "When creating ticket no exceptions is thrown"() {
        given:
        server.enqueue(new MockResponse().setResponseCode(HttpStatus.CREATED.value())
                .setResponseCode(HttpStatus.CREATED.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(new ClassPathResource("createTicketResponse.json").getFile().text)
        )

        when:
        zenDeskTicketService.createTicket(new Ticket())

        then:
        noExceptionThrown()
    }

    def "When invalid ticket is created an exception is thrown"() {
        server.enqueue(new MockResponse().setResponseCode(HttpStatus.UNPROCESSABLE_ENTITY.value()))

        when:
        zenDeskTicketService.createTicket(new Ticket()).block()

        then:
        thrown(WebClientResponseException)
    }
}
