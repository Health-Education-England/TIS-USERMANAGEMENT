package uk.nhs.hee.tis.usermanagement.exception;

public class UserCreationException extends RuntimeException {
  public UserCreationException() {
    super("Could not create user");
  }

  public UserCreationException(String message) {
    super(message);
  }
}
