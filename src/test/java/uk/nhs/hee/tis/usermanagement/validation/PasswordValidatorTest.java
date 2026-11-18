package uk.nhs.hee.tis.usermanagement.validation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.nhs.hee.tis.usermanagement.exception.PasswordException;

class PasswordValidatorTest {

  private final PasswordValidator passwordValidator = new PasswordValidator();

  @ParameterizedTest
  @MethodSource("mismatchedPasswordInputs")
  void shouldThrowWhenPasswordsDoNotMatch(String password, String confirmPassword) {
    PasswordException exception = assertThrows(PasswordException.class,
        () -> passwordValidator.validatePasswordsMatch(password, confirmPassword));

    assertEquals("Passwords do not match", exception.getMessage());
  }

  @ParameterizedTest
  @MethodSource("invalidLengthPasswords")
  void shouldThrowWhenPasswordIsTooShort(String password) {
    PasswordException exception = assertThrows(PasswordException.class,
        () -> passwordValidator.validatePasswordLength(password));

    assertEquals(String.format("Password needs to be at least %d chars long",
        PasswordValidator.REQUIRED_PASSWORD_LENGTH), exception.getMessage());
  }

  @Test
  void shouldNotThrowWhenPasswordsMatchAndLengthIsValid() {
    String password = "huntER2!";

    assertDoesNotThrow(() -> passwordValidator.validatePasswordsMatch(password, password));
    assertDoesNotThrow(() -> passwordValidator.validatePasswordLength(password));
  }

  private static Stream<Arguments> mismatchedPasswordInputs() {
    return Stream.of(
        Arguments.of("password1", "password2"),
        Arguments.of("password", null),
        Arguments.of(null, "password")
    );
  }

  private static Stream<Arguments> invalidLengthPasswords() {
    return Stream.of(
        Arguments.of((String) null),
        Arguments.of(""),
        Arguments.of("short"),
        Arguments.of("1234567")
    );
  }
}

