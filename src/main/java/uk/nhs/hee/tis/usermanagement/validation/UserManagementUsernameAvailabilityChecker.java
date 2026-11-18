package uk.nhs.hee.tis.usermanagement.validation;

import org.springframework.stereotype.Component;
import uk.nhs.hee.tis.usermanagement.facade.UserManagementFacade;

@Component
public class UserManagementUsernameAvailabilityChecker implements UsernameAvailabilityChecker {

  private final UserManagementFacade userManagementFacade;

  public UserManagementUsernameAvailabilityChecker(UserManagementFacade userManagementFacade) {
    this.userManagementFacade = userManagementFacade;
  }

  @Override
  public boolean isUsernameAvailable(String username) {
    return userManagementFacade.getUserByNameIgnoreCase(username) == null;
  }
}

