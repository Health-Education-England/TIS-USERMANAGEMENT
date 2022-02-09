package uk.nhs.hee.tis.usermanagement.event;

import java.util.Objects;
import org.springframework.context.ApplicationEvent;
import uk.nhs.hee.tis.usermanagement.DTOs.AuthenticationUserDto;

public class DeleteAuthenticationUserRequestedEvent extends ApplicationEvent {

  private AuthenticationUserDto authenticationUser;
  private boolean publishDeleteProfileUserEvent;

  public DeleteAuthenticationUserRequestedEvent(AuthenticationUserDto authenticationUser,
      boolean publishDeleteProfileUserEvent) {
    super(authenticationUser);
    this.authenticationUser = authenticationUser;
    this.publishDeleteProfileUserEvent = publishDeleteProfileUserEvent;
  }

  public AuthenticationUserDto getAuthenticationUser() {
    return authenticationUser;
  }

  public boolean isPublishDeleteProfileUserEvent() {
    return publishDeleteProfileUserEvent;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DeleteAuthenticationUserRequestedEvent that = (DeleteAuthenticationUserRequestedEvent) o;
    return publishDeleteProfileUserEvent == that.publishDeleteProfileUserEvent &&
        Objects.equals(authenticationUser, that.authenticationUser);
  }

  @Override
  public int hashCode() {
    return Objects.hash(authenticationUser, publishDeleteProfileUserEvent);
  }
}
