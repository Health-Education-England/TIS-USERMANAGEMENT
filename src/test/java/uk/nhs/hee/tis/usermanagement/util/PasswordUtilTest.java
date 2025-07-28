package uk.nhs.hee.tis.usermanagement.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PasswordUtilTest {

  private PasswordUtil passwordUtil;

  @BeforeEach
  public void setup() {
    passwordUtil = new PasswordUtil(1, 1, 1, 1, 12);
  }

  @RepeatedTest(10)
  void testPasswordLength() {
    String pwd = passwordUtil.generatePassword();
    assertEquals(12, pwd.length());
  }

  @RepeatedTest(10)
  void testContainsUppercase() {
    String pwd = passwordUtil.generatePassword();
    assertTrue(pwd.chars().anyMatch(Character::isUpperCase));
  }

  @RepeatedTest(10)
  void testContainsLowercase() {
    String pwd = passwordUtil.generatePassword();
    assertTrue(pwd.chars().anyMatch(Character::isLowerCase));
  }

  @RepeatedTest(10)
  void testContainsDigit() {
    String pwd = passwordUtil.generatePassword();
    assertTrue(pwd.chars().anyMatch(Character::isDigit));
  }

  @RepeatedTest(10)
  void testContainsSpecialChar() {
    String pwd = passwordUtil.generatePassword();
    String allowedSpecialChars = "^$*.[]{}()?-\"!@#%&/\\,><':;|_~`+=";
    assertTrue(pwd.chars().anyMatch(ch -> allowedSpecialChars.indexOf(ch) >= 0));
  }
}
