package uk.nhs.hee.tis.usermanagement.validation;

import org.springframework.stereotype.Component;
import uk.nhs.hee.tis.usermanagement.facade.UserManagementFacade;

/**
 * Username availability checker.
 */
@Component
public class UserManagementUsernameAvailabilityChecker implements UsernameAvailabilityChecker {

  private final UserManagementFacade userManagementFacade;

  public UserManagementUsernameAvailabilityChecker(UserManagementFacade userManagementFacade) {
    this.userManagementFacade = userManagementFacade;
  }

  /**
   * Check if the username does not already resolve to an existing user.
   *
   * @param username The username to check
   * @return {@code true} when no existing user is found
   */
  @Override
  public boolean isUsernameAvailable(String username) {
    return userManagementFacade.getUserByNameIgnoreCase(username) == null;
  }
}

