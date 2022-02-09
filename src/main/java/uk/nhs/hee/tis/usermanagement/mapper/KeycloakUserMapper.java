package uk.nhs.hee.tis.usermanagement.mapper;

import com.transform.hee.tis.keycloak.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.nhs.hee.tis.usermanagement.DTOs.AuthenticationUserDto;

@Mapper(componentModel = "spring", uses = {User.class})
public interface KeycloakUserMapper {

  @Mapping(source = "firstname", target = "givenName")
  @Mapping(source = "surname", target = "familyName")
  @Mapping(source = "tempPassword", target = "temporaryPassword")
  AuthenticationUserDto toAuthenticationUser(User keycloakUser);

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
