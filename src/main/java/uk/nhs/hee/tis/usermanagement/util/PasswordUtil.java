package uk.nhs.hee.tis.usermanagement.util;


import java.util.Arrays;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * The password util class.
 */
@Component
public class PasswordUtil {

  private final int PASSWORD_UPPERCASE_MINIMUM;

  private final int PASSWORD_LOWERCASE_MINIMUM;

  private final int PASSWORD_DIGIT_MINIMUM;

  private final int PASSWORD_SPECIAL_MINIMUM;

  private final int PASSWORD_LENGTH_MINIMUM;

  public PasswordUtil(
      @Value("${application.password-requirement.uppercaseMinimum}") int passwordUppercaseMinimum,
      @Value("${application.password-requirement.lowercaseMinimum}") int passwordLowercaseMinimum,
      @Value("${application.password-requirement.digitMinimum}") int passwordDigitMinimum,
      @Value("${application.password-requirement.specialMinimum}") int passwordSpecialMinimum,
      @Value("${application.password-requirement.lengthMinimum}") int passwordLengthMinimum) {
    PASSWORD_UPPERCASE_MINIMUM = passwordUppercaseMinimum;
    PASSWORD_LOWERCASE_MINIMUM = passwordLowercaseMinimum;
    PASSWORD_DIGIT_MINIMUM = passwordDigitMinimum;
    PASSWORD_SPECIAL_MINIMUM = passwordSpecialMinimum;
    PASSWORD_LENGTH_MINIMUM = passwordLengthMinimum;
  }

  /**
   * Generate a random password to match the AWS default requirements.
   *
   * @return generated password
   */
  public String generatePassword() {
    PasswordGenerator generator = new PasswordGenerator();
    CharacterRule upper = new CharacterRule(EnglishCharacterData.UpperCase,
        PASSWORD_UPPERCASE_MINIMUM);
    CharacterRule lower = new CharacterRule(EnglishCharacterData.LowerCase,
        PASSWORD_LOWERCASE_MINIMUM);
    CharacterRule digit = new CharacterRule(EnglishCharacterData.Digit, PASSWORD_SPECIAL_MINIMUM);
    CharacterRule special = new CharacterRule(EnglishCharacterData.SpecialAscii,
        PASSWORD_DIGIT_MINIMUM);

    return generator.generatePassword(PASSWORD_LENGTH_MINIMUM,
        Arrays.asList(upper, lower, digit, special));
  }
}
