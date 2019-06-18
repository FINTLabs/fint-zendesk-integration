package no.fint;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import no.fint.provisioning.model.TicketSynchronizationObject;
import no.fint.provisioning.model.UserSynchronizationObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Configuration
public class ApplicationConfiguration {

    @Value("${fint.zendesk.base-url:https://fintlabs.zendesk.com/api/v2/}")
    private String zenDeskBaseUrl;

    @Value("${fint.zendesk.username}")
    private String username;

    @Value("${fint.zendesk.token}")
    private String token;

    @Getter
    @Value("${fint.zendesk.user.sync.max-retry-attempts:10}")
    private int userSyncMaxRetryAttempts;

    @Getter
    @Value("${fint.zendesk.ticket.sync.max-retry-attempts:10}")
    private int ticketSyncMaxRetryAttempts;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(zenDeskBaseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE)
                .filter(ExchangeFilterFunctions.basicAuthentication(username, token))
                .filter(logRequest())
                .filter(logResponse())
                .build();
    }

    @Bean
    public BlockingQueue<UserSynchronizationObject> userSynchronizeQueue() {
        return new LinkedBlockingQueue<>();
    }

    @Bean
    public BlockingQueue<String> userDeleteQueue() {
        return new LinkedBlockingQueue<>();
    }

    @Bean
    public BlockingQueue<TicketSynchronizationObject> ticketQueue() {
        return new LinkedBlockingQueue<>();
    }


    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            log.info("\t{}", clientResponse.statusCode());
            return Mono.just(clientResponse);
        });
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.debug("\tRequest: {} {}", clientRequest.method(), clientRequest.url());
            clientRequest.headers().forEach((name, values) -> values.forEach(value -> log.debug("\t{}={}", name, value)));
            return Mono.just(clientRequest);
        });
    }
}
