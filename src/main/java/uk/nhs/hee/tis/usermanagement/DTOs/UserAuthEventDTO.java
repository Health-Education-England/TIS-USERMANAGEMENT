package uk.nhs.hee.tis.usermanagement.DTOs;

import java.util.Date;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserAuthEventDTO {
    private String eventId;
    private String eventType;
    private Date creationDate;
    private String eventResponse;
    private String challenges;
    private String device;
}