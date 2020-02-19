package no.fint.zendesk;

import lombok.Getter;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

@Service
public class RateLimiter {

    @Getter
    private volatile int remaining;

    public ExchangeFilterFunction rateLimiter() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            clientResponse.headers().header("X-Rate-Limit-Remaining").stream().mapToInt(Integer::parseInt).forEach(i -> remaining = i);
            return Mono.just(clientResponse);
        });
    }


}
