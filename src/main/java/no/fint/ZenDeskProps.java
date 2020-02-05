package no.fint;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "fint.zendesk")
public class ZenDeskProps {

    private List<TicketType> ticketTypes;
    private List<TicketPriority> ticketPriorities;
    private List<TicketCategory> ticketCategory;

    @Data
    public static class TicketType {
        private String name;
        private String value;
        private String help;
    }

    @Data
    public static class TicketPriority {
        private String name;
        private String value;
        private String help;
    }

    @Data
    public static class TicketCategory {
        private String name;
        private String help;
    }

    @Data
    public static class TicketCategoryOption {
        private String description;
        private String dn;
        private String basePath;
    }
}