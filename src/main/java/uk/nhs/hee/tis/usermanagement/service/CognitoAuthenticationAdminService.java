package uk.nhs.hee.tis.usermanagement.service;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.AdminCreateUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminCreateUserResult;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import uk.nhs.hee.tis.usermanagement.DTOs.AuthenticationUserDto;
import uk.nhs.hee.tis.usermanagement.DTOs.CreateUserDTO;
import uk.nhs.hee.tis.usermanagement.DTOs.UserDTO;
import uk.nhs.hee.tis.usermanagement.mapper.CognitoRequestMapper;
import uk.nhs.hee.tis.usermanagement.mapper.CognitoResultMapper;

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

  @Override
  void deleteUser(AuthenticationUserDto authenticationUser) {

  }
}
