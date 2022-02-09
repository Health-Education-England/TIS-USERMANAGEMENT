package uk.nhs.hee.tis.usermanagement.event;

import com.transform.hee.tis.keycloak.User;
import com.transformuk.hee.tis.profile.service.dto.HeeUserDTO;
import org.springframework.context.ApplicationEvent;

import java.util.Objects;
import uk.nhs.hee.tis.usermanagement.DTOs.AuthenticationUserDto;

public class CreateProfileUserRequestedEvent extends ApplicationEvent {

  private HeeUserDTO heeUserDTO;
  private AuthenticationUserDto authenticationUser;

  public CreateProfileUserRequestedEvent(HeeUserDTO heeUserDTO, AuthenticationUserDto authenticationUser) {
    super(heeUserDTO);
    this.heeUserDTO = heeUserDTO;
    this.authenticationUser = authenticationUser;
  }

  public HeeUserDTO getHeeUserDTO() {
    return heeUserDTO;
  }

  public AuthenticationUserDto getAuthenticationUser() {
    return authenticationUser;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CreateProfileUserRequestedEvent that = (CreateProfileUserRequestedEvent) o;
    return Objects.equals(heeUserDTO, that.heeUserDTO) &&
        Objects.equals(authenticationUser, that.authenticationUser);
  }

  @Override
  public int hashCode() {
    return Objects.hash(heeUserDTO, authenticationUser);
  }
}
