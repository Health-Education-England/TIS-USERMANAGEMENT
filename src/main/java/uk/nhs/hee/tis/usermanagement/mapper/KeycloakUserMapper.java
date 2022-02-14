package uk.nhs.hee.tis.usermanagement.mapper;

import com.transform.hee.tis.keycloak.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.nhs.hee.tis.usermanagement.DTOs.AuthenticationUserDto;

/**
 * A mapper to convert to/from a Keycloak {@link User} object.
 */
@Mapper(componentModel = "spring", uses = {User.class})
public interface KeycloakUserMapper {

  /**
   * Convert a keycloak user to an authentication user.
   *
   * @param keycloakUser The keycloak user to convert.
   * @return The converted authentication user.
   */
  @Mapping(source = "firstname", target = "givenName")
  @Mapping(source = "surname", target = "familyName")
  @Mapping(source = "tempPassword", target = "temporaryPassword")
  AuthenticationUserDto toAuthenticationUser(User keycloakUser);

  /**
   * Convert an authentication user to a keycloak user.
   *
   * @param authenticationUser The authentication user to convert.
   * @return The converted keycloak user.
   */
  default User toKeycloakUser(AuthenticationUserDto authenticationUser) {
    return User.create(
        authenticationUser.getId(),
        authenticationUser.getGivenName(),
        authenticationUser.getFamilyName(),
        authenticationUser.getUsername(),
        authenticationUser.getEmail(),
        authenticationUser.getPassword(),
        authenticationUser.isTemporaryPassword(),
        authenticationUser.getAttributes(),
        authenticationUser.isEnabled()
    );
  }
}
