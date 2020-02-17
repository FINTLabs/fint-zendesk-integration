package no.fint.provisioning

import groovy.json.JsonOutput
import no.fint.portal.model.contact.Contact
import no.fint.provisioning.model.UserSynchronizationObject
import no.fint.test.utils.MockMvcSpecification
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc

import java.util.concurrent.BlockingQueue

class UsersControllerSpec extends MockMvcSpecification {
    private MockMvc mockMvc
    private UsersController controller
    private BlockingQueue<UserSynchronizationObject> userSynchronizationQueue = Mock()

    void setup() {
        controller = new UsersController(userSynchronizeQueue: userSynchronizationQueue)
        mockMvc = standaloneSetup(controller)
    }

    def 'Update user'() {
        given:
        def body = JsonOutput.toJson(new Contact(dn: 'dn=jalla'))

        when:
        def response = mockMvc.perform(post('/users').contentType(MediaType.APPLICATION_JSON_UTF8).content(body))

        then:
        response.andExpect(status().isNoContent())
        1 * userSynchronizationQueue.offer({ it.operation == UserSynchronizationObject.Operation.UPDATE }, _, _) >> true
    }

    def 'Update user queue failure'() {
        given:
        def body = JsonOutput.toJson(new Contact(dn: 'dn=jalla'))

        when:
        def response = mockMvc.perform(post('/users').contentType(MediaType.APPLICATION_JSON_UTF8).content(body))

        then:
        response.andExpect(status().isUnprocessableEntity())
        1 * userSynchronizationQueue.offer(_ as UserSynchronizationObject, _, _) >> false
    }

    def 'Delete user'() {
        given:
        def body = JsonOutput.toJson(new Contact(dn: 'dn=jalla'))

        when:
        def response = mockMvc.perform(delete('/users').contentType(MediaType.APPLICATION_JSON_UTF8).content(body))

        then:
        response.andExpect(status().isNoContent())
        1 * userSynchronizationQueue.offer({ it.operation == UserSynchronizationObject.Operation.DELETE }, _, _) >> true
    }
}
