package uk.nhs.hee.tis.usermanagement.exception;

public class UserDeletionException extends RuntimeException {

  public UserDeletionException(String username) {
    super("Could not delete user: " + username);
  }
}
