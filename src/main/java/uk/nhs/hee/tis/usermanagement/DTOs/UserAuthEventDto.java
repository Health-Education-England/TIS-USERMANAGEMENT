package uk.nhs.hee.tis.usermanagement.DTOs;

import java.util.Date;
import lombok.Builder;
import lombok.Data;

/**
 * A DTO representing auth event logs for a user.
 */
@Data
@Builder
public class UserAuthEventDto {

  private String eventId;
  private String eventType;
  private Date creationDate;
  private String eventResponse;
  private String challenges;
  private String device;
}