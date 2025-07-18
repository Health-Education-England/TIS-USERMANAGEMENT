package uk.nhs.hee.tis.usermanagement.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminDeleteUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminDisableUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminEnableUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminListUserAuthEventsRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminListUserAuthEventsResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminSetUserPasswordRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminUpdateUserAttributesRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InvalidParameterException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserType;
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
  private final CognitoIdentityProviderClient cognitoClient;
  private final String userPoolId;

  private final CognitoRequestMapper requestMapper;
  private final CognitoResultMapper resultMapper;
  private final AuthenticationUserMapper userMapper;
  private final EmailService emailService;

  CognitoAuthenticationAdminService(ApplicationEventPublisher applicationEventPublisher,
      CognitoIdentityProviderClient cognitoClient,
      @Value("${application.cognito.user-pool-id}") String userPoolId,
      CognitoRequestMapper requestMapper, CognitoResultMapper resultMapper,
      AuthenticationUserMapper userMapper,
      EmailService emailService) {
    super(applicationEventPublisher);
    this.cognitoClient = cognitoClient;
    this.userPoolId = userPoolId;
    this.requestMapper = requestMapper;
    this.resultMapper = resultMapper;
    this.userMapper = userMapper;
    this.emailService = emailService;
  }

  @Override
  public String getServiceName() {
    return SERVICE_NAME;
  }

  @Override
  AuthenticationUserDto createUser(CreateUserDTO createUserDto) {
    AdminCreateUserRequest request = requestMapper.toCreateUserRequest(createUserDto);
    List<AttributeType> allAttributes = new ArrayList<>(request.userAttributes());
    allAttributes.add(AttributeType.builder().name(EMAIL_VERIFIED_FIELD).value(EMAIL_VERIFIED_VALUE)
        .build());
    request = request.toBuilder().userPoolId(userPoolId).userAttributes(allAttributes).build();

    AdminCreateUserResponse result = cognitoClient.adminCreateUser(request);

    // Users are enabled by default, disable if required.
    if (!createUserDto.isActive()) {
      AdminDisableUserRequest disableRequest = AdminDisableUserRequest.builder()
          .userPoolId(userPoolId)
          .username(result.user().username())
          .build();
      cognitoClient.adminDisableUser(disableRequest);
    }

    return resultMapper.toAuthenticationUser(result);
  }

  @Override
  public Optional<AuthenticationUserDto> getUser(String username) {
    final String emailFilter = String.format("email=\"%s\"", username);

    ListUsersRequest request = ListUsersRequest.builder()
        .userPoolId(userPoolId)
        .filter(emailFilter)
        .build();

    try {
      ListUsersResponse result = cognitoClient.listUsers(request);
      List<UserType> users = result.users();

      if (users == null || users.isEmpty()) {
        log.info("No user found with email: {}", username);
        return Optional.empty();
      }

      if (users.size() > 1) {
        log.warn("Multiple users found with email: {}", username);
        return Optional.empty();
      }

      return Optional.of(resultMapper.toAuthenticationUser(result.users().get(0)));
    } catch (InvalidParameterException e) {
      log.warn("Invalid parameter when querying user with email {}: {}", username, e.getMessage(),
          e);
      return Optional.empty();
    }
  }

  @Override
  public boolean updateUser(AuthenticationUserDto authenticationUser) {

    try {
      if (authenticationUser.isEnabled()) {
        AdminEnableUserRequest enableRequest = AdminEnableUserRequest.builder()
            .userPoolId(userPoolId)
            .username(authenticationUser.getUsername())
            .build();
        cognitoClient.adminEnableUser(enableRequest);
      } else {
        AdminDisableUserRequest disableRequest = AdminDisableUserRequest.builder()
            .userPoolId(userPoolId)
            .username(authenticationUser.getUsername())
            .build();
        cognitoClient.adminDisableUser(disableRequest);
      }

      AdminUpdateUserAttributesRequest request =
          requestMapper.toUpdateUserRequest(authenticationUser).toBuilder()
              .userPoolId(userPoolId).build();
      cognitoClient.adminUpdateUserAttributes(request);

      return true;
    } catch (CognitoIdentityProviderException e) {
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
    AdminSetUserPasswordRequest request = AdminSetUserPasswordRequest.builder()
        .userPoolId(userPoolId)
        .username(userId)
        .password(password)
        .permanent(!tempPassword)
        .build();
    cognitoClient.adminSetUserPassword(request);
    emailService.sendTemporaryPasswordEmail(userId, password);
    return true;
  }

  @Override
  void deleteUser(AuthenticationUserDto authenticationUser) {
    AdminDeleteUserRequest request = AdminDeleteUserRequest.builder()
        .userPoolId(userPoolId)
        .username(authenticationUser.getUsername())
        .build();
    cognitoClient.adminDeleteUser(request);
  }

  @Override
  public List<UserAuthEventDto> getUserAuthEvents(String username)
      throws RuntimeException {
    AdminListUserAuthEventsRequest request = AdminListUserAuthEventsRequest.builder()
        .userPoolId(userPoolId)
        .username(username)
        .maxResults(MAX_AUTH_EVENTS)
        .build();
    AdminListUserAuthEventsResponse result = cognitoClient.adminListUserAuthEvents(request);
    return resultMapper.toUserAuthEventDtos(result.authEvents());
  }
}
