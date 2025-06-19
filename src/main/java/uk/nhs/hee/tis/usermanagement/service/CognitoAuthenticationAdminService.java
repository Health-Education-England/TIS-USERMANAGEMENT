package uk.nhs.hee.tis.usermanagement.service;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.AWSCognitoIdentityProviderException;
import com.amazonaws.services.cognitoidp.model.AdminCreateUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminCreateUserResult;
import com.amazonaws.services.cognitoidp.model.AdminDeleteUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminDisableUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminEnableUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminListUserAuthEventsRequest;
import com.amazonaws.services.cognitoidp.model.AdminListUserAuthEventsResult;
import com.amazonaws.services.cognitoidp.model.AdminUpdateUserAttributesRequest;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.amazonaws.services.cognitoidp.model.InvalidParameterException;
import com.amazonaws.services.cognitoidp.model.ListUsersRequest;
import com.amazonaws.services.cognitoidp.model.ListUsersResult;
import com.amazonaws.services.cognitoidp.model.UserType;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import uk.nhs.hee.tis.usermanagement.DTOs.AuthenticationUserDto;
import uk.nhs.hee.tis.usermanagement.DTOs.CreateUserDTO;
import uk.nhs.hee.tis.usermanagement.DTOs.UserAuthEventDto;
import uk.nhs.hee.tis.usermanagement.DTOs.UserDTO;
import uk.nhs.hee.tis.usermanagement.mapper.AuthenticationUserMapper;
import uk.nhs.hee.tis.usermanagement.mapper.CognitoRequestMapper;
import uk.nhs.hee.tis.usermanagement.mapper.CognitoResultMapper;

/**
 * An authentication admin service implementation for Cognito.
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "application.authentication-provider", havingValue = "cognito")
public class CognitoAuthenticationAdminService extends AbstractAuthenticationAdminService {

  protected static final String EMAIL_VERIFIED_FIELD = "email_verified";
  protected static final String EMAIL_VERIFIED_VALUE = "true";
  protected static final int MAX_AUTH_EVENTS = 20;
  private static final String SERVICE_NAME = "cognito";
  private final AWSCognitoIdentityProvider cognitoClient;
  private final String userPoolId;

  private final CognitoRequestMapper requestMapper;
  private final CognitoResultMapper resultMapper;
  private final AuthenticationUserMapper userMapper;

  CognitoAuthenticationAdminService(ApplicationEventPublisher applicationEventPublisher,
      AWSCognitoIdentityProvider cognitoClient,
      @Value("${application.cognito.user-pool-id}") String userPoolId,
      CognitoRequestMapper requestMapper, CognitoResultMapper resultMapper,
      AuthenticationUserMapper userMapper) {
    super(applicationEventPublisher);
    this.cognitoClient = cognitoClient;
    this.userPoolId = userPoolId;
    this.requestMapper = requestMapper;
    this.resultMapper = resultMapper;
    this.userMapper = userMapper;
  }

  @Override
  public String getServiceName() {
    return SERVICE_NAME;
  }

  @Override
  AuthenticationUserDto createUser(CreateUserDTO createUserDto) {
    AdminCreateUserRequest request = requestMapper.toCreateUserRequest(createUserDto)
        .withUserPoolId(userPoolId)
        .withUserAttributes(
            new AttributeType().withName(EMAIL_VERIFIED_FIELD).withValue(EMAIL_VERIFIED_VALUE));

    AdminCreateUserResult result = cognitoClient.adminCreateUser(request);

    // Users are enabled by default, disable if required.
    if (!createUserDto.isActive()) {
      AdminDisableUserRequest disableRequest = new AdminDisableUserRequest()
          .withUserPoolId(userPoolId)
          .withUsername(result.getUser().getUsername());
      cognitoClient.adminDisableUser(disableRequest);
    }

    return resultMapper.toAuthenticationUser(result);
  }

  @Override
  public Optional<AuthenticationUserDto> getUser(String username) {
    final String emailFilter = String.format("email=\"%s\"", username);

    ListUsersRequest request = new ListUsersRequest()
        .withUserPoolId(userPoolId)
        .withFilter(emailFilter);

    try {
      ListUsersResult result = cognitoClient.listUsers(request);
      List<UserType> users = result.getUsers();

      if (users == null || users.isEmpty()) {
        log.info("No user found with email: {}", username);
        return Optional.empty();
      }

      if (users.size() > 1) {
        log.warn("Multiple users found with email: {}", username);
        return Optional.empty();
      }

      return Optional.of(resultMapper.toAuthenticationUser(result.getUsers().get(0)));
    } catch (InvalidParameterException e) {
      log.warn("Invalid parameter when querying user with email {}: {}", username, e.getMessage(), e);
      return Optional.empty();
    }
  }

  @Override
  public boolean updateUser(AuthenticationUserDto authenticationUser) {

    try {
      if (authenticationUser.isEnabled()) {
        AdminEnableUserRequest enableRequest = new AdminEnableUserRequest()
            .withUserPoolId(userPoolId)
            .withUsername(authenticationUser.getUsername());
        cognitoClient.adminEnableUser(enableRequest);
      } else {
        AdminDisableUserRequest disableRequest = new AdminDisableUserRequest()
            .withUserPoolId(userPoolId)
            .withUsername(authenticationUser.getUsername());
        cognitoClient.adminDisableUser(disableRequest);
      }

      AdminUpdateUserAttributesRequest request =
          requestMapper.toUpdateUserRequest(authenticationUser)
              .withUserPoolId(userPoolId);
      cognitoClient.adminUpdateUserAttributes(request);

      return true;
    } catch (AWSCognitoIdentityProviderException e) {
      log.error(e.getMessage(), e);
      return false;
    }
  }

  @Override
  public boolean updateUser(UserDTO userDto) {
    AuthenticationUserDto authenticationUser = userMapper.toAuthenticationUser(userDto);
    return updateUser(authenticationUser);
  }

  @Override
  public boolean updatePassword(String userId, String password, boolean tempPassword) {
    throw new UnsupportedOperationException("Users must reset their own password.");
  }

  @Override
  void deleteUser(AuthenticationUserDto authenticationUser) {
    AdminDeleteUserRequest request = new AdminDeleteUserRequest()
        .withUserPoolId(userPoolId)
        .withUsername(authenticationUser.getUsername());

    cognitoClient.adminDeleteUser(request);
  }

  @Override
  public List<UserAuthEventDto> getUserAuthEvents(String username)
      throws RuntimeException {
    AdminListUserAuthEventsRequest request = new AdminListUserAuthEventsRequest()
        .withUserPoolId(userPoolId)
        .withUsername(username)
        .withMaxResults(MAX_AUTH_EVENTS);
    AdminListUserAuthEventsResult result = cognitoClient.adminListUserAuthEvents(request);
    return resultMapper.toUserAuthEventDtos(result.getAuthEvents());
  }
}
