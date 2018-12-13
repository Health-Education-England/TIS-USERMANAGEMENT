package uk.nhs.hee.tis.usermanagement.event;

import com.transform.hee.tis.keycloak.User;
import com.transformuk.hee.tis.profile.service.dto.HeeUserDTO;
import org.springframework.context.ApplicationEvent;

import java.util.Objects;

public class CreateProfileUserRequestedEvent extends ApplicationEvent {

  private HeeUserDTO heeUserDTO;
  private User kcUser;

  public CreateProfileUserRequestedEvent(HeeUserDTO heeUserDTO, User kcUser) {
    super(heeUserDTO);
    this.heeUserDTO = heeUserDTO;
    this.kcUser = kcUser;
  }

  public HeeUserDTO getHeeUserDTO() {
    return heeUserDTO;
  }

  public User getKcUser() {
    return kcUser;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CreateProfileUserRequestedEvent that = (CreateProfileUserRequestedEvent) o;
    return Objects.equals(heeUserDTO, that.heeUserDTO) &&
        Objects.equals(kcUser, that.kcUser);
  }

  @Override
  public int hashCode() {
    return Objects.hash(heeUserDTO, kcUser);
  }
}
