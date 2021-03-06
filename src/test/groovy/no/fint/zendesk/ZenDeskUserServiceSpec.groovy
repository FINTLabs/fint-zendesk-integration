package no.fint.zendesk

import no.fint.portal.model.contact.Contact
import no.fint.portal.model.organisation.OrganisationService
import no.fint.provisioning.model.UserSynchronizationObject
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import spock.lang.Specification

class ZenDeskUserServiceSpec extends Specification {

    private def server = new MockWebServer()
    private def organisationService = Mock(OrganisationService)
    private def zenDeskUserService = new ZenDeskUserService(
            webClient: WebClient.create(server.url('/').toString()),
            organisationService: organisationService)
    private def contact = new Contact(nin: 12345678987, firstName: "Ola", lastName: "Olsen", mobile: "99999999", mail: "ola@olsen.net")
    private def userSynchronizationObject = new UserSynchronizationObject(contact)

    def "When creating a user no exceptions is thrown"() {
        given:
        server.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.CREATED.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(new ClassPathResource("createUserResponse.json").getFile().text)
        )

        when:
        zenDeskUserService.createOrUpdateZenDeskUser(contact)

        then:
        noExceptionThrown()
    }

    def "When creating a user with existing identity an exception is thrown"() {
        given:
        server.enqueue(new MockResponse().setResponseCode(HttpStatus.UNPROCESSABLE_ENTITY.value()))

        when:
        zenDeskUserService.createOrUpdateZenDeskUser(contact).block()

        then:
        thrown(WebClientResponseException)
    }

    def "When deleting user existing user no exceptions is thrown"() {
        given:
        server.enqueue(new MockResponse().setResponseCode(HttpStatus.OK.value()))

        when:
        zenDeskUserService.deleteZenDeskUser("123")

        then:
        noExceptionThrown()
    }

    def "When deleting a non existing user an exception is thrown"() {
        given:
        server.enqueue(new MockResponse().setResponseCode(HttpStatus.NOT_FOUND.value()))

        when:
        zenDeskUserService.deleteZenDeskUser("123").block()

        then:
        thrown(WebClientResponseException)
    }

    def "Contact 2 ZenDesk user"() {

        when:
        def user = zenDeskUserService.contactToZenDeskUser(contact)

        then:
        user.email == contact.getMail()
        user.phone == contact.getMobile()
    }

    def "Get signature"() {

        when:
        def signature = zenDeskUserService.getSignature(contact)

        then:
        signature != null
        signature.contains(contact.getLastName())
    }

    def "Get fullname"() {

        when:
        def fullname = zenDeskUserService.getFullname(contact)

        then:
        fullname != null
        fullname.contains(contact.firstName)
        fullname.contains(contact.lastName)

    }
}
