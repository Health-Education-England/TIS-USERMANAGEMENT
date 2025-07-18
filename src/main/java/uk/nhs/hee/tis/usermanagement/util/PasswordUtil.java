package uk.nhs.hee.tis.usermanagement.util;


import java.util.Arrays;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;

/**
 * The password util class.
 */
public class PasswordUtil {

  private PasswordUtil() {}

  /**
   * Generate a random password to match the AWS default requirements.
   *
   * @return generated password
   */
  public static String generatePassword() {
    PasswordGenerator generator = new PasswordGenerator();
    CharacterRule upper = new CharacterRule(EnglishCharacterData.UpperCase, 1);
    CharacterRule lower = new CharacterRule(EnglishCharacterData.LowerCase, 1);
    CharacterRule digit = new CharacterRule(EnglishCharacterData.Digit, 1);
    CharacterRule special = new CharacterRule(EnglishCharacterData.SpecialAscii, 1);

    return generator.generatePassword(12, Arrays.asList(upper, lower, digit, special));
  }
}
