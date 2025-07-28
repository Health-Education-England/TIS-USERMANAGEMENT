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

  private final int passwordUppercaseMinimum;

  private final int passwordLowercaseMinimum;

  private final int passwordDigitMinimum;

  private final int passwordSpecialMinimum;

  private final int passwordLengthMinimum;

  public PasswordUtil(
      @Value("${application.password-requirement.uppercaseMinimum}") int passwordUppercaseMinimum,
      @Value("${application.password-requirement.lowercaseMinimum}") int passwordLowercaseMinimum,
      @Value("${application.password-requirement.digitMinimum}") int passwordDigitMinimum,
      @Value("${application.password-requirement.specialMinimum}") int passwordSpecialMinimum,
      @Value("${application.password-requirement.lengthMinimum}") int passwordLengthMinimum) {
    this.passwordUppercaseMinimum = passwordUppercaseMinimum;
    this.passwordLowercaseMinimum = passwordLowercaseMinimum;
    this.passwordDigitMinimum = passwordDigitMinimum;
    this.passwordSpecialMinimum = passwordSpecialMinimum;
    this.passwordLengthMinimum = passwordLengthMinimum;
  }

  /**
   * Generate a random password to match the AWS default requirements.
   *
   * @return generated password
   */
  public String generatePassword() {
    PasswordGenerator generator = new PasswordGenerator();
    CharacterRule upper = new CharacterRule(EnglishCharacterData.UpperCase,
        passwordUppercaseMinimum);
    CharacterRule lower = new CharacterRule(EnglishCharacterData.LowerCase,
        passwordLowercaseMinimum);
    CharacterRule digit = new CharacterRule(EnglishCharacterData.Digit, passwordSpecialMinimum);
    CharacterRule special = new CharacterRule(EnglishCharacterData.SpecialAscii,
        passwordDigitMinimum);

    return generator.generatePassword(passwordLengthMinimum,
        Arrays.asList(upper, lower, digit, special));
  }
}
