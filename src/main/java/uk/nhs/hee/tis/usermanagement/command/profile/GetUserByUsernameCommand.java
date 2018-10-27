package uk.nhs.hee.tis.usermanagement.command.profile;

import com.transformuk.hee.tis.profile.client.service.impl.ProfileServiceImpl;
import com.transformuk.hee.tis.profile.service.dto.HeeUserDTO;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class GetUserByUsernameCommand extends ProfileHystrixCommand<Optional<HeeUserDTO>> {

  private static final Logger LOG = LoggerFactory.getLogger(GetPaginatedUsersCommand.class);

  private ProfileServiceImpl profileServiceImpl;
  private String username;
  private Throwable throwable;

  public GetUserByUsernameCommand(ProfileServiceImpl profileServiceImpl, String username) {
    this.profileServiceImpl = profileServiceImpl;
    this.username = username;
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
      HeeUserDTO userDto = profileServiceImpl.getSingleAdminUser(username);
      return Optional.of(userDto);
    } catch (Throwable e) {
      this.throwable = e;
      throw e;
    }
  }
}
