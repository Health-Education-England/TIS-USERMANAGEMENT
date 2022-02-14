package uk.nhs.hee.tis.usermanagement.event;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;
import uk.nhs.hee.tis.usermanagement.DTOs.AuthenticationUserDto;

@EqualsAndHashCode(callSuper = false)
@Getter
@ToString
public class DeleteAuthenticationUserRequestedEvent extends ApplicationEvent {

  private final AuthenticationUserDto authenticationUser;
  private final boolean publishDeleteProfileUserEvent;

  /**
   * Construct an event requesting a user is deleted by the authentication provider.
   *
   * @param authenticationUser            The authentication provider user to delete.
   * @param publishDeleteProfileUserEvent Whether to publish an event to also delete from profile.
   */
  public DeleteAuthenticationUserRequestedEvent(AuthenticationUserDto authenticationUser,
      boolean publishDeleteProfileUserEvent) {
    super(authenticationUser);
    this.authenticationUser = authenticationUser;
    this.publishDeleteProfileUserEvent = publishDeleteProfileUserEvent;
  }
}
