package uk.nhs.hee.tis.usermanagement.command.profile;

import com.transformuk.hee.tis.profile.client.service.ProfileService;
import com.transformuk.hee.tis.profile.service.dto.HeeUserDTO;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetUserByUsernameCommand extends ProfileHystrixCommand<Optional<HeeUserDTO>> {

  private static final Logger LOG = LoggerFactory.getLogger(GetPaginatedUsersCommand.class);

  private ProfileService profileService;
  private String username;
  private boolean caseIgnore;
  private Throwable throwable;

  public GetUserByUsernameCommand(ProfileService profileService, String username,
      boolean caseIgnore) {
    this.profileService = profileService;
    this.username = username;
    this.caseIgnore = caseIgnore;
  }

  @Override
  protected Optional<HeeUserDTO> getFallback() {
    LOG.warn(
        "An error occurred while getting a user from profile service, returning an empty optional as fallback");
    LOG.debug("Data that was sent, username: [{}]", username);
    LOG.warn("Exception: [{}]", ExceptionUtils.getStackTrace(throwable));
    return Optional.empty();
  }

  @Override
  protected Optional<HeeUserDTO> run() {
    try {
      if (!caseIgnore) {
        HeeUserDTO userDto = profileService.getSingleAdminUser(username);
        return Optional.ofNullable(userDto);
      } else {
        List<HeeUserDTO> userDTOs = profileService.getUsersByNameIgnoreCase(username);
        if (userDTOs.size() > 0) {
          return Optional.of(userDTOs.get(0));
        } else {
          return Optional.empty();
        }
      }
    } catch (Throwable e) {
      this.throwable = e;
      throw e;
    }
  }
}
