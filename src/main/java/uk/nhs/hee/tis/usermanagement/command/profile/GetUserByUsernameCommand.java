package uk.nhs.hee.tis.usermanagement.command.profile;

import com.transformuk.hee.tis.profile.client.service.impl.ProfileServiceImpl;
import com.transformuk.hee.tis.profile.service.dto.HeeUserDTO;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class GetUserByUsernameCommand extends ProfileHystrixCommand<Optional<HeeUserDTO>> {

  private static final Logger LOG = LoggerFactory.getLogger(GetPaginatedUsersCommand.class);

  private ProfileServiceImpl profileServiceImpl;
  private String username;
  private boolean caseIgnore;
  private Throwable throwable;

  public GetUserByUsernameCommand(ProfileServiceImpl profileServiceImpl, String username, boolean caseIgnore) {
    this.profileServiceImpl = profileServiceImpl;
    this.username = username;
    this.caseIgnore = caseIgnore;
  }

  @Override
  protected Optional<HeeUserDTO> getFallback() {
    LOG.warn("An error occurred while getting a user from profile service, returning an empty optional as fallback");
    LOG.debug("Data that was sent, username: [{}]", username);
    LOG.warn("Exception: [{}]", ExceptionUtils.getStackTrace(throwable));
    return Optional.empty();
  }

  @Override
  protected Optional<HeeUserDTO> run() throws Exception {
    try {
      if (this.caseIgnore == false) {
        HeeUserDTO userDto = profileServiceImpl.getSingleAdminUser(username);
        return Optional.of(userDto);
      } else {
        List<HeeUserDTO> userDTOs = profileServiceImpl.getUsersByNameIgnoreCase(username);
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
