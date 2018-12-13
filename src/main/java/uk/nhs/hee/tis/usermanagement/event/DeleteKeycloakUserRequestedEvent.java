package uk.nhs.hee.tis.usermanagement.event;

import com.transform.hee.tis.keycloak.User;
import org.springframework.context.ApplicationEvent;

import java.util.Objects;

public class DeleteKeycloakUserRequestedEvent extends ApplicationEvent {

  private User kcUser;
  private boolean publishDeleteProfileUserEvent;

  public DeleteKeycloakUserRequestedEvent(User kcUser, boolean publishDeleteProfileUserEvent) {
    super(kcUser);
    this.kcUser = kcUser;
    this.publishDeleteProfileUserEvent = publishDeleteProfileUserEvent;
  }

  public User getKcUser() {
    return kcUser;
  }

  public boolean isPublishDeleteProfileUserEvent() {
    return publishDeleteProfileUserEvent;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DeleteKeycloakUserRequestedEvent that = (DeleteKeycloakUserRequestedEvent) o;
    return publishDeleteProfileUserEvent == that.publishDeleteProfileUserEvent &&
        Objects.equals(kcUser, that.kcUser);
  }

  @Override
  public int hashCode() {
    return Objects.hash(kcUser, publishDeleteProfileUserEvent);
  }
}
