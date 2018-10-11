package uk.nhs.hee.tis.usermanagement.command.profile;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.transformuk.hee.tis.profile.client.service.impl.ProfileServiceImpl;
import com.transformuk.hee.tis.profile.service.dto.HeeUserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class GetUserByUsernameCommand extends HystrixCommand<Optional<HeeUserDTO>> {

  private static final String COMMAND_KEY = "PROFILE_COMMANDS";
  private static final Logger LOG = LoggerFactory.getLogger(GetPaginatedUsersCommand.class);

  private ProfileServiceImpl profileServiceImpl;
  private String username;

  public GetUserByUsernameCommand(ProfileServiceImpl profileServiceImpl, String username) {
    super(HystrixCommandGroupKey.Factory.asKey(COMMAND_KEY));
    this.profileServiceImpl = profileServiceImpl;
    this.username = username;
  }

  @Override
  protected Optional<HeeUserDTO> getFallback() {
    LOG.warn("An error occurred while getting a user from profile service, returning an empty optional as fallback");
    LOG.debug("Data that was sent, username: [{}]", username);
    return Optional.empty();
  }

  @Override
  protected Optional<HeeUserDTO> run() throws Exception {
    try {
      HeeUserDTO userDto = profileServiceImpl.getSingleAdminUser(username);
      return Optional.of(userDto);
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }
}
