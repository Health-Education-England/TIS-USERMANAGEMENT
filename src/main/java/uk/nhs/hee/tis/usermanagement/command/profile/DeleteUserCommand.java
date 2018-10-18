package uk.nhs.hee.tis.usermanagement.command.profile;

import com.google.gson.Gson;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.transformuk.hee.tis.profile.client.service.impl.ProfileServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteUserCommand extends HystrixCommand<Boolean> {

  private static final String COMMAND_KEY = "PROFILE_COMMANDS";
  private static final String HEE_USERS_ENDPOINT = "/api/hee-users";
  private static final Logger LOG = LoggerFactory.getLogger(CreateUserCommand.class);
  private static final Gson GSON = new Gson();

  private ProfileServiceImpl profileServiceImpl;
  private String username;
  private Throwable throwable;

  public DeleteUserCommand(ProfileServiceImpl profileServiceImpl, String username) {
    super(HystrixCommandGroupKey.Factory.asKey(COMMAND_KEY));
    this.profileServiceImpl = profileServiceImpl;
    this.username = username;
  }

  @Override
  protected Boolean getFallback() {
    LOG.warn("An error was thrown while attempting to delete user [{}] from the Profile service", this.username);
    return false;
  }

  @Override
  protected Boolean run() throws Exception {
    try {
      return profileServiceImpl.deleteUser(username);
    } catch (Throwable e) {
      this.throwable = e;
      throw e;
    }
  }
}
