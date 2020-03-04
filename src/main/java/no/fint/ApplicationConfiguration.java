package no.fint;

import lombok.Getter;
import no.fint.provisioning.model.TicketSynchronizationObject;
import no.fint.provisioning.model.UserSynchronizationObject;
import no.fint.zendesk.RateLimiter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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
    public WebClient webClient(RateLimiter rateLimiter, RequestLogger requestLogger) {
        return WebClient.builder()
                .baseUrl(zenDeskBaseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE)
                .filter(ExchangeFilterFunctions.basicAuthentication(username, token))
                .filter(rateLimiter.rateLimiter())
                .filter(requestLogger.logRequest())
                .filter(requestLogger.logResponse())
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

}
