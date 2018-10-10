package uk.nhs.hee.tis.usermanagement.exception;

public class UserNotFoundException extends RuntimeException {

  public UserNotFoundException(String username, String serviceName) {
    super("Could not find user: [" + username + "] in the " + serviceName + " service");
  }
}
