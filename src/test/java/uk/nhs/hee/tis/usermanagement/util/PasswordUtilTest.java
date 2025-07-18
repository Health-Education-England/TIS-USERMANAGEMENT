package uk.nhs.hee.tis.usermanagement.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.RepeatedTest;

class PasswordUtilTest {

  @RepeatedTest(10)
  void testPasswordLength() {
    String pwd = PasswordUtil.generatePassword();
    assertEquals(12, pwd.length());
  }

  @RepeatedTest(10)
  void testContainsUppercase() {
    String pwd = PasswordUtil.generatePassword();
    assertTrue(pwd.chars().anyMatch(Character::isUpperCase));
  }

  @RepeatedTest(10)
  void testContainsLowercase() {
    String pwd = PasswordUtil.generatePassword();
    assertTrue(pwd.chars().anyMatch(Character::isLowerCase));
  }

  @RepeatedTest(10)
  void testContainsDigit() {
    String pwd = PasswordUtil.generatePassword();
    assertTrue(pwd.chars().anyMatch(Character::isDigit));
  }

  @RepeatedTest(10)
  void testContainsSpecialChar() {
    String pwd = PasswordUtil.generatePassword();
    String allowedSpecialChars = "^$*.[]{}()?-\"!@#%&/\\,><':;|_~`+=";
    assertTrue(pwd.chars().anyMatch(ch -> allowedSpecialChars.indexOf(ch) >= 0));
  }
}
