package uk.nhs.hee.tis.usermanagement.validation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.nhs.hee.tis.usermanagement.exception.PasswordException;

/**
 * Validates password-related constraints for user operations.
 */
@Component
public class PasswordValidator {

  public static final int REQUIRED_PASSWORD_LENGTH = 8;

  /**
   * Validate that password and confirmation password are identical.
   *
   * @param password The password value
   * @param confirmPassword The confirmation password value
   * @throws PasswordException If values do not match
   */
  public void validatePasswordsMatch(String password, String confirmPassword) {
    if (!StringUtils.equals(password, confirmPassword)) {
      throw new PasswordException("Passwords do not match");
    }
  }

  /**
   * Validate the minimum password length requirement.
   *
   * @param password The password value
   * @throws PasswordException If password is empty or too short
   */
  public void validatePasswordLength(String password) {
    if (StringUtils.isEmpty(password) || password.length() < REQUIRED_PASSWORD_LENGTH) {
      throw new PasswordException(String.format(
          "Password needs to be at least %d chars long", REQUIRED_PASSWORD_LENGTH));
    }
  }
}
