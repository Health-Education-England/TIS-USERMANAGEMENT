package uk.nhs.hee.tis.usermanagement.service;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.AWSCognitoIdentityProviderException;
import com.amazonaws.services.cognitoidp.model.AdminCreateUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminCreateUserResult;
import com.amazonaws.services.cognitoidp.model.AdminDeleteUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminGetUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminGetUserResult;
import com.amazonaws.services.cognitoidp.model.AdminSetUserPasswordRequest;
import com.amazonaws.services.cognitoidp.model.UserNotFoundException;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import uk.nhs.hee.tis.usermanagement.DTOs.AuthenticationUserDto;
import uk.nhs.hee.tis.usermanagement.DTOs.CreateUserDTO;
import uk.nhs.hee.tis.usermanagement.DTOs.UserDTO;
import uk.nhs.hee.tis.usermanagement.mapper.CognitoRequestMapper;
import uk.nhs.hee.tis.usermanagement.mapper.CognitoResultMapper;

@Slf4j
@Service
@ConditionalOnProperty(name = "application.authentication-provider", havingValue = "cognito")
public class CognitoAuthenticationAdminService extends AbstractAuthenticationAdminService {

  private static final String SERVICE_NAME = "cognito";

  private final AWSCognitoIdentityProvider cognitoClient;
  private final String userPoolId;

  private final CognitoRequestMapper requestMapper;
  private final CognitoResultMapper resultMapper;

  CognitoAuthenticationAdminService(ApplicationEventPublisher applicationEventPublisher,
      AWSCognitoIdentityProvider cognitoClient,
      @Value("${application.cognito.user-pool-id}") String userPoolId,
      CognitoRequestMapper requestMapper, CognitoResultMapper resultMapper) {
    super(applicationEventPublisher);
    this.cognitoClient = cognitoClient;
    this.userPoolId = userPoolId;
    this.requestMapper = requestMapper;
    this.resultMapper = resultMapper;
  }

  @Override
  public String getServiceName() {
    return SERVICE_NAME;
  }

  @Override
  AuthenticationUserDto createUser(CreateUserDTO createUserDto) {
    AdminCreateUserRequest request = requestMapper.toCreateUserRequest(createUserDto)
        .withUserPoolId(userPoolId);

    AdminCreateUserResult result = cognitoClient.adminCreateUser(request);
    return resultMapper.toAuthenticationUser(result);
  }

  @Override
  public Optional<AuthenticationUserDto> getUser(String username) {
    AdminGetUserRequest request = new AdminGetUserRequest()
        .withUserPoolId(userPoolId)
        .withUsername(username);

    try {
      AdminGetUserResult result = cognitoClient.adminGetUser(request);
      return Optional.of(resultMapper.toAuthenticationUser(result));
    } catch (UserNotFoundException e) {
      log.warn(e.getMessage());
      return Optional.empty();
    }
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
    AdminSetUserPasswordRequest request = new AdminSetUserPasswordRequest()
        .withUserPoolId(userPoolId)
        .withUsername(userId)
        .withPassword(password)
        .withPermanent(!tempPassword);

    try {
      cognitoClient.adminSetUserPassword(request);
    } catch (AWSCognitoIdentityProviderException e) {
      log.error(e.getMessage());
      return false;
    }

    return true;
  }

  @Override
  void deleteUser(AuthenticationUserDto authenticationUser) {
    AdminDeleteUserRequest request = new AdminDeleteUserRequest()
        .withUserPoolId(userPoolId)
        .withUsername(authenticationUser.getId());

    cognitoClient.adminDeleteUser(request);
  }
}
