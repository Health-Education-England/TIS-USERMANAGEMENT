package uk.nhs.hee.tis.usermanagement.mapper;

import com.transform.hee.tis.keycloak.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.nhs.hee.tis.usermanagement.DTOs.AuthenticationUserDto;
import uk.nhs.hee.tis.usermanagement.DTOs.UserDTO;

/**
 * A mapper to convert to/from an {@link AuthenticationUserDto} object.
 */
@Mapper(componentModel = "spring")
public interface AuthenticationUserMapper {

  /**
   * Convert a keycloak user to an authentication user.
   *
   * @param keycloakUser The keycloak user to convert.
   * @return The converted authentication user.
   */
  @Mapping(target = "givenName", source = "firstname")
  @Mapping(target = "familyName", source = "surname")
  @Mapping(target = "temporaryPassword", source = "tempPassword")
  AuthenticationUserDto toAuthenticationUser(User keycloakUser);

  /**
   * Convert a user dto to an authentication user.
   *
   * @param userDto The user dto to convert.
   * @return The converted authentication user.
   */
  @Mapping(target = "id", source = "kcId")
  @Mapping(target = "username", source = "name")
  @Mapping(target = "givenName", source = "firstName")
  @Mapping(target = "familyName", source = "lastName")
  @Mapping(target = "enabled", source = "active")
  @Mapping(target = "email", source = "emailAddress")
  AuthenticationUserDto toAuthenticationUser(UserDTO userDto);

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
        authenticationUser.getTemporaryPassword(),
        authenticationUser.getAttributes(),
        authenticationUser.isEnabled()
    );
  }
}
