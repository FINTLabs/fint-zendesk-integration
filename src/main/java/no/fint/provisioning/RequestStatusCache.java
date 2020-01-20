package no.fint.provisioning;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import no.fint.provisioning.model.RequestStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Objects;

@Component
public class RequestStatusCache {
    private Cache<String, RequestStatus> cache;

    @Value("${no.fint.consumer.status-cache:expireAfterAccess=30m,expireAfterWrite=6h}")
    private String cacheSpec;

    @PostConstruct
    public void init() {
        cache = CacheBuilder.from(cacheSpec).build();
    }

    public boolean containsKey(String id) {
        return Objects.nonNull(cache.getIfPresent(id));
    }

    public RequestStatus get(String id) {
        return cache.getIfPresent(id);
    }

    public void put(String corrId, RequestStatus request) {
        cache.put(corrId, request);
    }

}