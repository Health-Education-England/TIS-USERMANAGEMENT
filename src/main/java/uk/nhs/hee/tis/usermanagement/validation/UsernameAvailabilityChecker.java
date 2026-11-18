package uk.nhs.hee.tis.usermanagement.validation;

public interface UsernameAvailabilityChecker {

  boolean isUsernameAvailable(String username);
}