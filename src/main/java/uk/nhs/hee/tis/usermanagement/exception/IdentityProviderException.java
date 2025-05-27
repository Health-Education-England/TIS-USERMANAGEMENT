package uk.nhs.hee.tis.usermanagement.exception;

/**
 * Exception to capture exceptions thrown during interactions with identify provider API.
 *
 */
public class IdentityProviderException extends RuntimeException {

  public IdentityProviderException(String message) {
    super(message);
  }
}