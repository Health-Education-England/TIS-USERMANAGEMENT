package uk.nhs.hee.tis.usermanagement.validation;

/**
 * Interface for checking whether a username is available for creation.
 */
public interface UsernameAvailabilityChecker {

  /**
   * Determine whether a username can be used for a new user.
   *
   * @param username The username to check
   * @return {@code true} if the username is available
   */
  boolean isUsernameAvailable(String username);
}