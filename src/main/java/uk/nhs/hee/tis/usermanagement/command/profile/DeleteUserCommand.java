package uk.nhs.hee.tis.usermanagement.command.profile;

import com.transformuk.hee.tis.profile.client.service.impl.ProfileServiceImpl;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteUserCommand extends ProfileHystrixCommand<Boolean> {

  private static final Logger LOG = LoggerFactory.getLogger(DeleteUserCommand.class);

  private ProfileServiceImpl profileServiceImpl;
  private String username;
  private Throwable throwable;

  public DeleteUserCommand(ProfileServiceImpl profileServiceImpl, String username) {
    this.profileServiceImpl = profileServiceImpl;
    this.username = username;
  }

  @Override
  protected Boolean getFallback() {
    LOG.warn("An error was thrown while attempting to delete user [{}] from the Profile service", this.username);
    LOG.warn("Exception: [{}]", ExceptionUtils.getStackTrace(throwable));
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
