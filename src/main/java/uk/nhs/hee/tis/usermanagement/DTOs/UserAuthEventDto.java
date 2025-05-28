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
  private String event;
  private Date eventDate;
  private String result;
  private String challenges;
  private String device;
}