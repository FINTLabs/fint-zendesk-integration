package no.fint;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import no.fint.provisioning.model.TicketSynchronizationObject;
import no.fint.provisioning.model.UserSynchronizationObject;
import no.fint.zendesk.RateLimiter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.client.reactive.ReactorResourceFactory;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Configuration
@Slf4j
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
    public WebClient webClient(
            WebClient.Builder builder,
            ReactorResourceFactory factory,
            ConnectionProvider connectionProvider,
            RateLimiter rateLimiter,
            RequestLogger requestLogger) {
        factory.setConnectionProvider(connectionProvider);
        return builder
                .clientConnector(new ReactorClientHttpConnector(factory, HttpClient::secure))
                .baseUrl(zenDeskBaseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE)
                .filter(ExchangeFilterFunctions.basicAuthentication(username, token))
                .filter(rateLimiter.rateLimiter())
                .filter(requestLogger.logRequest())
                .filter(requestLogger.logResponse())
                .build();
    }

    @Bean
    public ConnectionProvider connectionProvider(ConnectionProviderSettings settings) {
        log.info("Connection Provider settings: {}", settings);
        switch (StringUtils.upperCase(settings.getType())) {
            case "FIXED":
                return ConnectionProvider.fixed("Zendesk", settings.getMaxConnections(), settings.getAcquireTimeout());
            case "ELASTIC":
                return ConnectionProvider.elastic("Zendesk");
            case "NEW":
                return ConnectionProvider.newConnection();
            default:
                throw new IllegalArgumentException("Illegal connection provider type: " + settings.getType());
        }
    }

    @Bean
    public BlockingQueue<UserSynchronizationObject> userSynchronizeQueue(
            @Value("${fint.zendesk.user.queue:0}") int queueLength
    ) {
        if (queueLength <= 0) {
            return new LinkedBlockingQueue<>();
        }
        return new LinkedBlockingQueue<>(queueLength);
    }

    @Bean
    public BlockingQueue<String> userDeleteQueue(
            @Value("${fint.zendesk.delete.queue:0}") int queueLength
    ) {
        if (queueLength <= 0) {
            return new LinkedBlockingQueue<>();
        }
        return new LinkedBlockingQueue<>(queueLength);
    }

    @Bean
    public BlockingQueue<TicketSynchronizationObject> ticketQueue(
            @Value("${fint.zendesk.ticket.queue:0}") int queueLength
    ) {
        if (queueLength <= 0) {
            return new LinkedBlockingQueue<>();
        }
        return new LinkedBlockingQueue<>(queueLength);
    }

}
