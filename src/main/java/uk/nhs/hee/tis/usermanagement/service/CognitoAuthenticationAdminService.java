package uk.nhs.hee.tis.usermanagement.service;

import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.nhs.hee.tis.usermanagement.DTOs.AuthenticationUserDto;
import uk.nhs.hee.tis.usermanagement.DTOs.UserDTO;

@Service
@ConditionalOnProperty(name = "application.authentication-provider", havingValue = "cognito")
public class CognitoAuthenticationAdminService implements AuthenticationAdminService {

  @Override
  public String getServiceName() {
    return null;
  }

  @Override
  public Optional<AuthenticationUserDto> getUser(String username) {
    return Optional.empty();
  }

  @Override
  public boolean updateUser(AuthenticationUserDto authenticationUser) {
    return false;
  }

  @Override
  public boolean updateUser(UserDTO userDto) {
    return false;
  }

  @Override
  public boolean updatePassword(String userId, String password, boolean tempPassword) {
    return false;
  }
}
