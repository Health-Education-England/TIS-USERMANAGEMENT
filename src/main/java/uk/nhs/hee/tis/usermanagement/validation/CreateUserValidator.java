package uk.nhs.hee.tis.usermanagement.validation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.hee.tis.usermanagement.DTOs.CreateUserDTO;
import uk.nhs.hee.tis.usermanagement.exception.UserCreationException;
import uk.nhs.hee.tis.usermanagement.facade.UserManagementFacade;

/**
 * Validates create-user request fields before creating a user.
 */
@Component
public class CreateUserValidator {

  private final UserManagementFacade userManagementFacade;

  @Autowired
  public CreateUserValidator(UserManagementFacade userManagementFacade) {
    this.userManagementFacade = userManagementFacade;
  }

  /**
   * Validate username and email formatting constraints.
   *
   * @param user The create-user request payload
   * @throws UserCreationException If username or email contains whitespace
   */
  public void validateNameAndEmail(CreateUserDTO user) {
    if (StringUtils.containsWhitespace(user.getName())) {
      throw new UserCreationException(
          "Cannot create user, username shouldn't contain white spaces");
    }
    if (StringUtils.containsWhitespace(user.getEmailAddress())) {
      throw new UserCreationException(
          "Cannot create user, email address shouldn't contain white spaces");
    }
  }

  /**
   * Validate that the username is not already in use.
   *
   * @param username The username to check
   * @throws UserCreationException If the username already exists
   */
  public void validateUsernameAvailable(String username) {
    if (userManagementFacade.getUserByNameIgnoreCase(username) != null) {
      throw new UserCreationException("Cannot create user, the username already exists");
    }
  }
}
