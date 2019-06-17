package no.fint.zendesk.model;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class ZenDeskUser {
    private boolean shared;
    private String lastLoginAt;
    private String role;
    private String notes;
    private String signature;
    private boolean sharedAgent;
    private String customRoleId;
    private boolean moderator;
    private String createdAt;
    private String externalId;
    private String locale;
    private long defaultGroupId;
    private int localeId;
    private UserFields userFields;
    private String updatedAt;
    private boolean reportCsv;
    private String alias;
    private String details;
    private long id;
    private String email;
    private boolean restrictedAgent;
    private boolean twoFactorAuthEnabled;
    private String roleType;
    private boolean onlyPrivateComments;
    private String ianaTimeZone;
    private String sharedPhoneNumber;
    private boolean verified;
    private Photo photo;
    private boolean active;
    private String timeZone;
    private String url;
    private boolean suspended;
    private List<String> tags;
    private String phone;
    private long organizationId;
    private String name;
    private String ticketRestriction;
    private boolean chatOnly;
}
