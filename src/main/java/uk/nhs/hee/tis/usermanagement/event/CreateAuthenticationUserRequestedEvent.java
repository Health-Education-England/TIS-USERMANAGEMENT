package uk.nhs.hee.tis.usermanagement.event;

import com.transformuk.hee.tis.profile.service.dto.HeeUserDTO;
import org.springframework.context.ApplicationEvent;
import uk.nhs.hee.tis.usermanagement.DTOs.CreateUserDTO;

import java.util.Objects;

public class CreateAuthenticationUserRequestedEvent extends ApplicationEvent {

  private CreateUserDTO userDTO;
  private HeeUserDTO userToCreateInProfileService;

  public CreateAuthenticationUserRequestedEvent(CreateUserDTO source, HeeUserDTO userToCreateInProfileService) {
    super(source);
    this.userDTO = source;
    this.userToCreateInProfileService = userToCreateInProfileService;
  }

  public CreateUserDTO getUserDTO() {
    return userDTO;
  }

  public HeeUserDTO getUserToCreateInProfileService() {
    return userToCreateInProfileService;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CreateAuthenticationUserRequestedEvent that = (CreateAuthenticationUserRequestedEvent) o;
    return Objects.equals(userDTO, that.userDTO) &&
        Objects.equals(userToCreateInProfileService, that.userToCreateInProfileService);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userDTO, userToCreateInProfileService);
  }
}
