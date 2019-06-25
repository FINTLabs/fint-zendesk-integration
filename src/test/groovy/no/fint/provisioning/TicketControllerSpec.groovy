package no.fint.provisioning

import com.fasterxml.jackson.databind.ObjectMapper
import no.fint.test.utils.MockMvcSpecification
import no.fint.zendesk.model.ticket.Comment
import no.fint.zendesk.model.ticket.Ticket
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc

import java.util.concurrent.LinkedBlockingQueue

class TicketControllerSpec extends MockMvcSpecification {
    private MockMvc mockMvc
    private TicketController controller
    private TicketQueuingService ticketQueuingService = Mock()

    void setup() {
        controller = new TicketController(ticketQueuingService: ticketQueuingService)
        mockMvc = standaloneSetup(controller)
    }

    def "Get ticket types"() {
        when:
        def response = mockMvc.perform(get('/tickets/type'))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPathEquals('$[0].name', 'Spørsmål'))
                .andExpect(jsonPathEquals('$[0].value', 'question'))
                .andExpect(jsonPathEquals('$[1].name', 'Hendelse'))
                .andExpect(jsonPathEquals('$[1].value', 'incident'))
                .andExpect(jsonPathEquals('$[2].name', 'Problem'))
                .andExpect(jsonPathEquals('$[2].value', 'problem'))
                .andExpect(jsonPathEquals('$[3].name', 'Oppgave'))
                .andExpect(jsonPathEquals('$[3].value', 'task'))
    }

    def "Post ticket"() {
        given:
        def ticket = new Ticket(id: 123, subject: 'subject', type: 'type', submitterId: 123, priority: 'priority',
                tags: [], requesterId: 123, comment: new Comment(body: 'body'))
        def json = new ObjectMapper().writeValueAsString(ticket)

        when:
        def response = mockMvc.perform(post('/tickets')
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE)
                .content(json))

        then:
        1 * ticketQueuingService.put(_)
        response.andExpect(status().isOk())
    }

    def "Get ticket priority"() {
        when:
        def response = mockMvc.perform(get('/tickets/priority'))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPathEquals('$[0].name', 'Lav'))
                .andExpect(jsonPathEquals('$[0].value', 'low'))
                .andExpect(jsonPathEquals('$[1].name', 'Høy'))
                .andExpect(jsonPathEquals('$[1].value', 'high'))
                .andExpect(jsonPathEquals('$[2].name', 'Haster'))
                .andExpect(jsonPathEquals('$[2].value', 'urgent'))
    }

    def "Get queue size"() {
        when:
        def response = mockMvc.perform(get('/tickets/queue/count'))

        then:
        1 * ticketQueuingService.ticketQueue >> new LinkedBlockingQueue<String>()
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$').value(0))
    }
}
