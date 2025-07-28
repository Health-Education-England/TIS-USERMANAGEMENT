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

  @Value("${application.password-requirement.uppercaseMinimum}")
  private int PASSWORD_UPPERCASE_MINIMUM;

  @Value("${application.password-requirement.lowercaseMinimum}")
  private int PASSWORD_LOWERCASE_MINIMUM;

  @Value("${application.password-requirement.digitMinimum}")
  private int PASSWORD_DIGIT_MINIMUM;

  @Value("${application.password-requirement.specialMinimum}")
  private int PASSWORD_SPECIAL_MINIMUM;

  @Value("${application.password-requirement.lengthMinimum}")
  private int PASSWORD_LENGTH_MINIMUM;

  @Value("${spring.profiles.active}")
  private String prop;

  public PasswordUtil() {}

  /**
   * Generate a random password to match the AWS default requirements.
   *
   * @return generated password
   */
  public String generatePassword() {
    PasswordGenerator generator = new PasswordGenerator();
    CharacterRule upper = new CharacterRule(EnglishCharacterData.UpperCase, PASSWORD_UPPERCASE_MINIMUM);
    CharacterRule lower = new CharacterRule(EnglishCharacterData.LowerCase, PASSWORD_LOWERCASE_MINIMUM);
    CharacterRule digit = new CharacterRule(EnglishCharacterData.Digit, PASSWORD_SPECIAL_MINIMUM);
    CharacterRule special = new CharacterRule(EnglishCharacterData.SpecialAscii, PASSWORD_DIGIT_MINIMUM);

    return generator.generatePassword(PASSWORD_LENGTH_MINIMUM, Arrays.asList(upper, lower, digit, special));
  }


}
