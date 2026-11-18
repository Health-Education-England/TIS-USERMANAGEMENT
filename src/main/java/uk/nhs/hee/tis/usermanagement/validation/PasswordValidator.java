package uk.nhs.hee.tis.usermanagement.validation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.nhs.hee.tis.usermanagement.exception.PasswordException;

@Component
public class PasswordValidator {

  public static final int REQUIRED_PASSWORD_LENGTH = 8;

  public void validatePasswordsMatch(String password, String confirmPassword) {
    if (!StringUtils.equals(password, confirmPassword)) {
      throw new PasswordException("Passwords do not match");
    }
  }

  public void validatePasswordLength(String password) {
    if (StringUtils.isEmpty(password) || password.length() < REQUIRED_PASSWORD_LENGTH) {
      throw new PasswordException(String.format(
          "Password needs to be at least %d chars long", REQUIRED_PASSWORD_LENGTH));
    }
  }
}
