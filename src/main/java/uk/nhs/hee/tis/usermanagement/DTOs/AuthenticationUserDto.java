package uk.nhs.hee.tis.usermanagement.DTOs;

import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * A DTO representing a generic user from an authentication provider.
 */
@Data
public class AuthenticationUserDto {

  private String id;
  private String username;
  private String givenName;
  private String familyName;
  private boolean enabled;
  private String email;
  private Map<String, List<String>> attributes;
  private String password;
  private Boolean temporaryPassword;
}
