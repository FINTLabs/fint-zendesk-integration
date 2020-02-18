package no.fint.zendesk.model.user;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class User {

    private String name;
    private String notes;
    private String signature;
    private String details;
    private Long id;
    private String email;
    private Boolean verified;
    private String phone;
    private String externalId;
    private String role;

    //private boolean shared;
    //private String lastLoginAt;
    //private boolean sharedAgent;
    //private String customRoleId;
    //private boolean moderator;
    //private String createdAt;
    //private String locale;
    //private long defaultGroupId;
    //private int localeId;
    //private UserFields userFields;
    //private String updatedAt;
    //private boolean reportCsv;
    //private String alias;
    //private boolean restrictedAgent;
    //private boolean twoFactorAuthEnabled;
    //private String roleType;
    //private boolean onlyPrivateComments;
    //private String ianaTimeZone;
    //private String sharedPhoneNumber;
    //private Photo photo;
    //private boolean active;
    //private String timeZone;
    //private String url;
    //private boolean suspended;
    //private List<String> tags;
    //private long organizationId;
    //private String ticketRestriction;
    //private boolean chatOnly;
}
