package no.fint.provisioning

import com.fasterxml.jackson.databind.ObjectMapper
import no.fint.ZenDeskProps
import no.fint.test.utils.MockMvcSpecification
import no.fint.zendesk.model.ticket.Comment
import no.fint.zendesk.model.ticket.Ticket
import no.fint.zendesk.model.ticket.TicketCategory
import no.fint.zendesk.model.ticket.TicketPriority
import no.fint.zendesk.model.ticket.TicketType
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc

import java.util.concurrent.LinkedBlockingQueue

class TicketControllerSpec extends MockMvcSpecification {
    private MockMvc mockMvc
    private TicketController controller
    private TicketQueuingService ticketQueuingService
    private ZenDeskProps zenDeskProps
    private StatusCache statusCache

    void setup() {
        ticketQueuingService = Mock()
        zenDeskProps = Mock()
        statusCache = Mock()
        controller = new TicketController(ticketQueuingService: ticketQueuingService, statusCache: statusCache,  zenDeskProps: zenDeskProps)
        mockMvc = standaloneSetup(controller)
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
        1 * ticketQueuingService.put(_) >> true
        response.andExpect(status().isAccepted())
    }

    def "Get ticket types"() {
        when:
        def response = mockMvc.perform(get('/tickets/type'))

        then:
        1 * zenDeskProps.getTicketTypes() >> [new TicketType(name: 'Spørsmål', value: 'question'), new TicketType(name: 'Hendelse', value: 'incident')]
        response.andExpect(status().isOk())
                .andExpect(jsonPathEquals('$[0].name', 'Spørsmål'))
                .andExpect(jsonPathEquals('$[0].value', 'question'))
                .andExpect(jsonPathEquals('$[1].name', 'Hendelse'))
                .andExpect(jsonPathEquals('$[1].value', 'incident'))
    }

    def "Get ticket category"() {
        when:
        def response = mockMvc.perform(get('/tickets/category'))

        then:
        1 * zenDeskProps.getTicketCategory() >> [
                new TicketCategory(name: 'QlikSense'),
                new TicketCategory(name: 'QlikView'),
                new TicketCategory(name: 'Hub'),
                new TicketCategory(name: 'NPrinting'),
                new TicketCategory(name: 'Analyse')]
        response.andExpect(status().isOk())
                .andExpect(jsonPathEquals('$[0].name', 'QlikSense'))
                .andExpect(jsonPathEquals('$[1].name', 'QlikView'))
                .andExpect(jsonPathEquals('$[2].name', 'Hub'))
                .andExpect(jsonPathEquals('$[3].name', 'NPrinting'))
                .andExpect(jsonPathEquals('$[4].name', 'Analyse'))
    }

    def "Get ticket priority"() {
        when:
        def response = mockMvc.perform(get('/tickets/priority'))

        then:
        1 * zenDeskProps.getTicketPriorities() >> [
                new TicketPriority(name: 'Lav', value: 'low'),
                new TicketPriority(name: 'Normal', value: 'normal'),
                new TicketPriority(name: 'Høy', value: 'high'),
                new TicketPriority(name: 'Haster', value: 'urgent')
        ]
        response.andExpect(status().isOk())
                .andExpect(jsonPathEquals('$[0].name', 'Lav'))
                .andExpect(jsonPathEquals('$[0].value', 'low'))
                .andExpect(jsonPathEquals('$[1].name', 'Normal'))
                .andExpect(jsonPathEquals('$[1].value', 'normal'))
                .andExpect(jsonPathEquals('$[2].name', 'Høy'))
                .andExpect(jsonPathEquals('$[2].value', 'high'))
                .andExpect(jsonPathEquals('$[3].name', 'Haster'))
                .andExpect(jsonPathEquals('$[3].value', 'urgent'))
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
