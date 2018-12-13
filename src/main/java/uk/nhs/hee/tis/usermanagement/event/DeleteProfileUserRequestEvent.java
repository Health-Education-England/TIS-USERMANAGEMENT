package uk.nhs.hee.tis.usermanagement.event;

import org.springframework.context.ApplicationEvent;

import java.util.Objects;

public class DeleteProfileUserRequestEvent extends ApplicationEvent {

  private String username;

  public DeleteProfileUserRequestEvent(String username) {
    super(username);
    this.username = username;
  }

  public String getUsername() {
    return username;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DeleteProfileUserRequestEvent that = (DeleteProfileUserRequestEvent) o;
    return Objects.equals(username, that.username);
  }

  @Override
  public int hashCode() {
    return Objects.hash(username);
  }
}
