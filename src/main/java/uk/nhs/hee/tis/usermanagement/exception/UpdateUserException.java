package uk.nhs.hee.tis.usermanagement.exception;

public class UpdateUserException extends RuntimeException {

  public UpdateUserException(String username, String serviceName) {
    super("Could not update user [" + username + "] in serviceName [" + serviceName + "]");
  }

  /**
   * Constructs a new UpdateUserException with the specified detail message.
   *
   * @param message the detail message
   */
  public UpdateUserException(String message) {
    super(message);
  }
}
