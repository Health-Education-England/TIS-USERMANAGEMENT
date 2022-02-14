package uk.nhs.hee.tis.usermanagement.event;

import com.transformuk.hee.tis.profile.service.dto.HeeUserDTO;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;
import uk.nhs.hee.tis.usermanagement.DTOs.AuthenticationUserDto;

@EqualsAndHashCode(callSuper = false)
@Getter
@ToString
public class CreateProfileUserRequestedEvent extends ApplicationEvent {

  private final HeeUserDTO heeUserDTO;
  private final AuthenticationUserDto authenticationUser;

  /**
   * Construct an event requesting a profile user is created.
   *
   * @param heeUserDTO         The profile user details.
   * @param authenticationUser The authentication provider user details.
   */
  public CreateProfileUserRequestedEvent(HeeUserDTO heeUserDTO,
      AuthenticationUserDto authenticationUser) {
    super(heeUserDTO);
    this.heeUserDTO = heeUserDTO;
    this.authenticationUser = authenticationUser;
  }
}
