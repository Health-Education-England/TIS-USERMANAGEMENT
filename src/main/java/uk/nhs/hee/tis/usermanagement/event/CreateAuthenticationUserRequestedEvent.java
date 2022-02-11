package uk.nhs.hee.tis.usermanagement.event;

import com.transformuk.hee.tis.profile.service.dto.HeeUserDTO;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;
import uk.nhs.hee.tis.usermanagement.DTOs.CreateUserDTO;

@EqualsAndHashCode(callSuper = false)
@Getter
@ToString
public class CreateAuthenticationUserRequestedEvent extends ApplicationEvent {

  private final CreateUserDTO userDTO;
  private final HeeUserDTO userToCreateInProfileService;

  /**
   * Construct an event requesting a user is created by the authentication provider.
   *
   * @param source                       The user to create.
   * @param userToCreateInProfileService The user profile details.
   */
  public CreateAuthenticationUserRequestedEvent(CreateUserDTO source,
      HeeUserDTO userToCreateInProfileService) {
    super(source);
    this.userDTO = source;
    this.userToCreateInProfileService = userToCreateInProfileService;
  }
}
