package uk.nhs.hee.tis.usermanagement.event;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

@EqualsAndHashCode(callSuper = false)
@Getter
@ToString
public class DeleteProfileUserRequestEvent extends ApplicationEvent {

  private final String username;

  /**
   * Construct an event requesting a profile user is deleted.
   *
   * @param username The username to delete.
   */
  public DeleteProfileUserRequestEvent(String username) {
    super(username);
    this.username = username;
  }
}
