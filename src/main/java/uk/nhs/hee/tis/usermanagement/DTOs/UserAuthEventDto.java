package uk.nhs.hee.tis.usermanagement.DTOs;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * A DTO representing auth event logs for a user.
 */
@Data
@Builder
public class UserAuthEventDto {

  private String eventId;
  private String event;
  private LocalDateTime eventDateTime;
  private String result;
  private String challenges;
  private String device;
}