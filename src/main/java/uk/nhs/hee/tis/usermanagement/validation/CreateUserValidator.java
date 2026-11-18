package uk.nhs.hee.tis.usermanagement.validation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.hee.tis.usermanagement.DTOs.CreateUserDTO;
import uk.nhs.hee.tis.usermanagement.exception.UserCreationException;

@Component
public class CreateUserValidator {

  private final UsernameAvailabilityChecker usernameAvailabilityChecker;

  @Autowired
  public CreateUserValidator(UsernameAvailabilityChecker usernameAvailabilityChecker) {
    this.usernameAvailabilityChecker = usernameAvailabilityChecker;
  }

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

  public void validateUsernameAvailable(String username) {
    if (usernameAvailabilityChecker != null && !usernameAvailabilityChecker.isUsernameAvailable(
        username)) {
      throw new UserCreationException("Cannot create user, the username already exists");
    }
  }
}
