package no.fint;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import reactor.netty.resources.ConnectionProvider;

@ConfigurationProperties("fint.webclient.connection-provider")
@Component
@Data
public class ConnectionProviderSettings {
    private String type = "fixed";
    private int maxConnections = ConnectionProvider.DEFAULT_POOL_MAX_CONNECTIONS;
    private long acquireTimeout = ConnectionProvider.DEFAULT_POOL_ACQUIRE_TIMEOUT;
}
