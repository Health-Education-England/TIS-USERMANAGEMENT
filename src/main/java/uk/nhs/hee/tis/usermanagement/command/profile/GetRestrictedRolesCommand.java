package uk.nhs.hee.tis.usermanagement.command.profile;

import com.transformuk.hee.tis.profile.client.service.ProfileService;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hystrix command to get all restricted roles from Profile service.
 */
public class GetRestrictedRolesCommand extends ProfileHystrixCommand<Set<String>> {

  private static final Logger LOG = LoggerFactory.getLogger(GetRestrictedRolesCommand.class);

  private final ProfileService profileService;
  private Exception exception;

  public GetRestrictedRolesCommand(ProfileService profileService) {
    this.profileService = profileService;
  }

  @Override
  protected Set<String> getFallback() {
    LOG.warn("An occurred while getting all restricted roles from Profile service, "
        + "returning an empty Set as fallback");
    LOG.warn("Exception: [{}]", exception.getStackTrace());
    return Set.of();
  }

  @Override
  protected Set<String> run() {
    try {
      return profileService.getRestrictedRoles();
    } catch (Exception e) {
      this.exception = e;
      throw e;
    }
  }
}
