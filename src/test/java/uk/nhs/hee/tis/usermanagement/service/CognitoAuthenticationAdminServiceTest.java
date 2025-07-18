package uk.nhs.hee.tis.usermanagement.service;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.nhs.hee.tis.usermanagement.service.CognitoAuthenticationAdminService.EMAIL_VERIFIED_FIELD;
import static uk.nhs.hee.tis.usermanagement.service.CognitoAuthenticationAdminService.EMAIL_VERIFIED_VALUE;
import static uk.nhs.hee.tis.usermanagement.service.CognitoAuthenticationAdminService.MAX_AUTH_EVENTS;

import com.transformuk.hee.tis.profile.service.dto.HeeUserDTO;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
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
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthEventType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ChallengeResponseType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.EventContextDataType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InvalidParameterException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserNotFoundException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserType;
import uk.nhs.hee.tis.usermanagement.DTOs.AuthenticationUserDto;
import uk.nhs.hee.tis.usermanagement.DTOs.CreateUserDTO;
import uk.nhs.hee.tis.usermanagement.DTOs.UserAuthEventDto;
import uk.nhs.hee.tis.usermanagement.DTOs.UserDTO;
import uk.nhs.hee.tis.usermanagement.event.CreateAuthenticationUserRequestedEvent;
import uk.nhs.hee.tis.usermanagement.event.CreateProfileUserRequestedEvent;
import uk.nhs.hee.tis.usermanagement.event.DeleteAuthenticationUserRequestedEvent;
import uk.nhs.hee.tis.usermanagement.event.DeleteProfileUserRequestEvent;
import uk.nhs.hee.tis.usermanagement.mapper.AuthenticationUserMapperImpl;
import uk.nhs.hee.tis.usermanagement.mapper.CognitoRequestMapperImpl;
import uk.nhs.hee.tis.usermanagement.mapper.CognitoResultMapperImpl;

@ExtendWith(MockitoExtension.class)
class CognitoAuthenticationAdminServiceTest {

  private static final String USER_POOL_ID = "region-1_userPool123";

  private static final String USERNAME = "anthony.gilliam";
  private static final String PASSWORD = "T0ny_123";

  private static final String EMAIL_FIELD = "email";
  private static final String EMAIL_VALUE = "anthony.gilliam@dummy.com";
  private static final String GIVEN_NAME_FIELD = "given_name";
  private static final String GIVEN_NAME_VALUE = "Anthony";
  private static final String FAMILY_NAME_FIELD = "family_name";
  private static final String FAMILY_NAME_VALUE = "Gilliam";
  private static final String SUB_FIELD = "sub";
  private static final String SUB_VALUE = UUID.randomUUID().toString();

  private CognitoAuthenticationAdminService service;

  private CognitoIdentityProviderClient cognitoClient;
  private ApplicationEventPublisher eventPublisher;
  @Mock
  private EmailService emailService;

  private static Stream<Exception> catchableExceptionProvider() {
    return Stream.of(
        InvalidParameterException.builder().message("Limited characters are permitted").build());
  }

  private static Stream<Collection<UserType>> userTypeCollectionProvider() {
    return Stream.of(null, Collections.emptyList());
  }

  @BeforeEach
  void setUp() {
    cognitoClient = mock(CognitoIdentityProviderClient.class);
    eventPublisher = mock(ApplicationEventPublisher.class);
    service = new CognitoAuthenticationAdminService(
        eventPublisher,
        cognitoClient,
        USER_POOL_ID,
        new CognitoRequestMapperImpl(),
        new CognitoResultMapperImpl(),
        new AuthenticationUserMapperImpl(),
        emailService
    );
  }

  @Test
  void shouldSendCreateUserRequest() {
    CreateUserDTO dto = new CreateUserDTO();
    dto.setName(USERNAME);
    dto.setEmailAddress(EMAIL_VALUE);
    dto.setFirstName(GIVEN_NAME_VALUE);
    dto.setLastName(FAMILY_NAME_VALUE);
    dto.setActive(true);
    // Password fields should never be set when connected to Cognito, but this is tested for completeness
    dto.setPassword(PASSWORD);
    dto.setTempPassword(true);

    CreateAuthenticationUserRequestedEvent event = new CreateAuthenticationUserRequestedEvent(dto,
        new HeeUserDTO());
    service.createUserEventListener(event);

    ArgumentCaptor<AdminCreateUserRequest> requestCaptor = ArgumentCaptor.forClass(
        AdminCreateUserRequest.class);
    verify(cognitoClient).adminCreateUser(requestCaptor.capture());

    AdminCreateUserRequest request = requestCaptor.getValue();
    assertThat("Unexpected user pool id.", request.userPoolId(), is(USER_POOL_ID));
    assertThat("Unexpected username.", request.username(), is(USERNAME));
    assertThat("Unexpected password.", request.temporaryPassword(), nullValue());

    List<AttributeType> attributes = request.userAttributes();
    assertThat("Unexpected attribute count.", attributes.size(), is(4));

    Map<String, String> attributeMap = attributes.stream()
        .collect(Collectors.toMap(AttributeType::name, AttributeType::value));
    assertThat("Unexpected email.", attributeMap.get(EMAIL_FIELD), is(EMAIL_VALUE));
    assertThat("Unexpected given name.", attributeMap.get(GIVEN_NAME_FIELD), is(GIVEN_NAME_VALUE));
    assertThat("Unexpected family name.", attributeMap.get(FAMILY_NAME_FIELD),
        is(FAMILY_NAME_VALUE));
    assertThat("Unexpected email verified.", attributeMap.get(EMAIL_VERIFIED_FIELD),
        is(EMAIL_VERIFIED_VALUE));
  }

  @Test
  void shouldNotDisableCreatedUserWhenActiveTrue() {
    CreateUserDTO dto = new CreateUserDTO();
    dto.setActive(true);

    AdminCreateUserResponse result = AdminCreateUserResponse.builder()
        .user(UserType.builder()
            .username(USERNAME)
            .attributes(Collections.emptyList())
            .build())
        .build();
    when(cognitoClient.adminCreateUser(any(AdminCreateUserRequest.class))).thenReturn(result);

    service.createUser(dto);

    verify(cognitoClient, never()).adminDisableUser(any(AdminDisableUserRequest.class));
  }

  @Test
  void shouldDisableCreatedUserWhenActiveFalse() {
    CreateUserDTO dto = new CreateUserDTO();
    dto.setActive(false);

    AdminCreateUserResponse result = AdminCreateUserResponse.builder()
        .user(UserType.builder()
            .username(USERNAME)
            .attributes(Collections.emptyList())
            .build())
        .build();
    when(cognitoClient.adminCreateUser(any(AdminCreateUserRequest.class))).thenReturn(result);

    service.createUser(dto);

    ArgumentCaptor<AdminDisableUserRequest> requestCaptor = ArgumentCaptor.forClass(
        AdminDisableUserRequest.class);
    verify(cognitoClient).adminDisableUser(requestCaptor.capture());

    AdminDisableUserRequest request = requestCaptor.getValue();
    assertThat("Unexpected user pool id.", request.userPoolId(), is(USER_POOL_ID));
    assertThat("Unexpected username.", request.username(), is(USERNAME));
  }

  @Test
  void shouldPublishCreatedUserWhenUserCreated() {
    UserType cognitoUser = UserType.builder()
        .username(USERNAME)
        .attributes(buildStandardCognitoAttributes())
        .enabled(true).build();

    AdminCreateUserResponse result = AdminCreateUserResponse.builder()
        .user(cognitoUser).build();

    when(cognitoClient.adminCreateUser(any(AdminCreateUserRequest.class))).thenReturn(result);

    CreateUserDTO createUserDto = new CreateUserDTO();
    createUserDto.setActive(true);

    CreateAuthenticationUserRequestedEvent event = new CreateAuthenticationUserRequestedEvent(
        createUserDto, new HeeUserDTO());
    service.createUserEventListener(event);

    ArgumentCaptor<CreateProfileUserRequestedEvent> eventCaptor = ArgumentCaptor.forClass(
        CreateProfileUserRequestedEvent.class);
    verify(eventPublisher).publishEvent(eventCaptor.capture());

    verifyAuthenticationUser(eventCaptor.getValue().getAuthenticationUser());
  }

  @Test
  void shouldPublishProfileUserToBeCreatedWhenUserCreated() {
    CreateUserDTO createUserDto = new CreateUserDTO();
    createUserDto.setActive(true);

    HeeUserDTO heeUser = new HeeUserDTO();

    CreateAuthenticationUserRequestedEvent event = new CreateAuthenticationUserRequestedEvent(
        createUserDto, heeUser);
    service.createUserEventListener(event);

    ArgumentCaptor<CreateProfileUserRequestedEvent> eventCaptor = ArgumentCaptor.forClass(
        CreateProfileUserRequestedEvent.class);
    verify(eventPublisher).publishEvent(eventCaptor.capture());

    assertThat("Unexpected hee user.", eventCaptor.getValue().getHeeUserDTO(),
        sameInstance(heeUser));
  }

  @Test
  void shouldSendGetCognitoUserRequest() {
    ListUsersResponse result = ListUsersResponse.builder().users(Collections.emptyList()).build();

    ArgumentCaptor<ListUsersRequest> requestCaptor = ArgumentCaptor.forClass(
        ListUsersRequest.class);
    when(cognitoClient.listUsers(requestCaptor.capture())).thenReturn(result);

    service.getUser(USERNAME);

    ListUsersRequest request = requestCaptor.getValue();
    assertThat("Unexpected user pool id.", request.userPoolId(), is(USER_POOL_ID));
    assertThat("Unexpected username.", request.filter(), containsString(USERNAME));
  }

  @Test
  void shouldReturnGetCognitoUserResultWhenUsernameFound() {
    UserType user = UserType.builder()
        .enabled(true)
        .username(USERNAME)
        .attributes(buildStandardCognitoAttributes())
        .build();
    ListUsersResponse result = ListUsersResponse.builder()
        .users(Collections.singletonList(user))
        .build();

    when(cognitoClient.listUsers(any(ListUsersRequest.class))).thenReturn(result);

    Optional<AuthenticationUserDto> optionalAuthenticationUser = service.getUser(USERNAME);

    assertThat("Expected user not found.", optionalAuthenticationUser.isPresent(), is(true));
    verifyAuthenticationUser(optionalAuthenticationUser.get());
  }

  @ParameterizedTest
  @MethodSource("userTypeCollectionProvider")
  void shouldReturnEmptyResultWhenUserNotFound(Collection<UserType> userTypes) {
    ListUsersResponse result = ListUsersResponse.builder().users(userTypes).build();

    when(cognitoClient.listUsers(any(ListUsersRequest.class))).thenReturn(result);
    Optional<AuthenticationUserDto> optionalAuthenticationUser = service.getUser(USERNAME);

    assertThat("Unexpected user found.", optionalAuthenticationUser.isPresent(), is(false));
  }

  @Test
  void shouldReturnEmptyResultWhenUsernameFoundMultiple() {
    UserType user1 = UserType.builder()
        .enabled(true)
        .username(USERNAME)
        .attributes(buildStandardCognitoAttributes())
        .build();
    UserType user2 = UserType.builder()
        .enabled(false)
        .username(USERNAME)
        .attributes(buildStandardCognitoAttributes())
        .build();
    ListUsersResponse result = ListUsersResponse.builder()
        .users(Arrays.asList(user1, user2)).build();

    when(cognitoClient.listUsers(any(ListUsersRequest.class))).thenReturn(result);
    Optional<AuthenticationUserDto> optionalAuthenticationUser = service.getUser(USERNAME);

    assertThat("Unexpected user found.", optionalAuthenticationUser.isPresent(), is(false));
  }

  @ParameterizedTest
  @MethodSource("catchableExceptionProvider")
  void shouldReturnEmptyGetCognitoUserResultWhenExceptionHappens(Exception e) {
    when(cognitoClient.listUsers(any(ListUsersRequest.class))).thenThrow(e);

    Optional<AuthenticationUserDto> optionalAuthenticationUser = service.getUser(USERNAME);

    assertThat("Unexpected user found.", optionalAuthenticationUser.isPresent(), is(false));
  }

  @Test
  void shouldSendUpdateCognitoUserRequestWhenAuthenticationUserProvided() {
    AuthenticationUserDto authenticationUser = new AuthenticationUserDto();
    authenticationUser.setUsername(USERNAME);
    authenticationUser.setGivenName(GIVEN_NAME_VALUE);
    authenticationUser.setFamilyName(FAMILY_NAME_VALUE);
    authenticationUser.setEmail(EMAIL_VALUE);
    authenticationUser.setAttributes(
        Collections.singletonMap("sub", singletonList(SUB_VALUE)));

    service.updateUser(authenticationUser);

    ArgumentCaptor<AdminUpdateUserAttributesRequest> requestCaptor = ArgumentCaptor.forClass(
        AdminUpdateUserAttributesRequest.class);
    verify(cognitoClient).adminUpdateUserAttributes(requestCaptor.capture());

    AdminUpdateUserAttributesRequest request = requestCaptor.getValue();
    assertThat("Unexpected user pool id.", request.userPoolId(), is(USER_POOL_ID));
    assertThat("Unexpected username.", request.username(), is(USERNAME));

    List<AttributeType> attributes = request.userAttributes();
    assertThat("Unexpected attribute count.", attributes.size(), is(3));

    Map<String, String> attributeMap = attributes.stream()
        .collect(Collectors.toMap(AttributeType::name, AttributeType::value));
    assertThat("Unexpected email.", attributeMap.get(EMAIL_FIELD), is(EMAIL_VALUE));
    assertThat("Unexpected given name.", attributeMap.get(GIVEN_NAME_FIELD), is(GIVEN_NAME_VALUE));
    assertThat("Unexpected family name.", attributeMap.get(FAMILY_NAME_FIELD),
        is(FAMILY_NAME_VALUE));
  }

  @Test
  void shouldEnableUserWhenUpdatingUserAndEnabledTrue() {
    AuthenticationUserDto authenticationUser = new AuthenticationUserDto();
    authenticationUser.setUsername(USERNAME);
    authenticationUser.setEnabled(true);

    service.updateUser(authenticationUser);

    ArgumentCaptor<AdminEnableUserRequest> requestCaptor = ArgumentCaptor.forClass(
        AdminEnableUserRequest.class);
    verify(cognitoClient).adminEnableUser(requestCaptor.capture());

    AdminEnableUserRequest request = requestCaptor.getValue();
    assertThat("Unexpected user pool id.", request.userPoolId(), is(USER_POOL_ID));
    assertThat("Unexpected username.", request.username(), is(USERNAME));
  }

  @Test
  void shouldDisableWhenUpdatingUserAndEnabledFalse() {
    AuthenticationUserDto authenticationUser = new AuthenticationUserDto();
    authenticationUser.setUsername(USERNAME);
    authenticationUser.setEnabled(false);

    service.updateUser(authenticationUser);

    ArgumentCaptor<AdminDisableUserRequest> requestCaptor = ArgumentCaptor.forClass(
        AdminDisableUserRequest.class);
    verify(cognitoClient).adminDisableUser(requestCaptor.capture());

    AdminDisableUserRequest request = requestCaptor.getValue();
    assertThat("Unexpected user pool id.", request.userPoolId(), is(USER_POOL_ID));
    assertThat("Unexpected username.", request.username(), is(USERNAME));
  }

  @Test
  void shouldOverwriteOriginalAttributesWhenUpdatingUser() {
    AuthenticationUserDto authenticationUser = new AuthenticationUserDto();
    authenticationUser.setUsername(USERNAME);
    authenticationUser.setGivenName(GIVEN_NAME_VALUE);
    authenticationUser.setFamilyName(FAMILY_NAME_VALUE);
    authenticationUser.setEmail(EMAIL_VALUE);

    Map<String, List<String>> originalAttributes = new HashMap<>();
    originalAttributes.put(SUB_FIELD, singletonList("original-value-123"));
    originalAttributes.put(GIVEN_NAME_FIELD, singletonList("originalValue1"));
    originalAttributes.put(FAMILY_NAME_FIELD, singletonList("originalValue2"));
    originalAttributes.put(EMAIL_FIELD, singletonList("originalValue3"));
    authenticationUser.setAttributes(originalAttributes);

    service.updateUser(authenticationUser);

    ArgumentCaptor<AdminUpdateUserAttributesRequest> requestCaptor = ArgumentCaptor.forClass(
        AdminUpdateUserAttributesRequest.class);
    verify(cognitoClient).adminUpdateUserAttributes(requestCaptor.capture());

    List<AttributeType> attributes = requestCaptor.getValue().userAttributes();
    assertThat("Unexpected attribute count.", attributes.size(), is(3));

    Map<String, String> attributeMap = attributes.stream()
        .collect(Collectors.toMap(AttributeType::name, AttributeType::value));
    assertThat("Unexpected email.", attributeMap.get(EMAIL_FIELD), is(EMAIL_VALUE));
    assertThat("Unexpected given name.", attributeMap.get(GIVEN_NAME_FIELD), is(GIVEN_NAME_VALUE));
    assertThat("Unexpected family name.", attributeMap.get(FAMILY_NAME_FIELD),
        is(FAMILY_NAME_VALUE));
  }

  @Test
  void shouldReturnTrueWhenUserUpdatedByAuthenticationUser() {
    boolean updated = service.updateUser(new AuthenticationUserDto());

    assertThat("Unexpected updated state.", updated, is(true));
  }

  @Test
  void shouldReturnFalseWhenUserUpdateFailsByAuthenticationUser() {
    when(cognitoClient.adminUpdateUserAttributes(any(AdminUpdateUserAttributesRequest.class)))
        .thenThrow(CognitoIdentityProviderException.builder().message("Dummy Exception.").build());

    boolean updated = service.updateUser(new AuthenticationUserDto());

    assertThat("Unexpected updated state.", updated, is(false));
  }

  @Test
  void shouldSendUpdateCognitoUserRequestWhenUserDtoProvided() {
    UserDTO userDto = new UserDTO();
    userDto.setName(USERNAME);
    userDto.setFirstName(GIVEN_NAME_VALUE);
    userDto.setLastName(FAMILY_NAME_VALUE);
    userDto.setEmailAddress(EMAIL_VALUE);

    service.updateUser(userDto);

    ArgumentCaptor<AdminUpdateUserAttributesRequest> requestCaptor = ArgumentCaptor.forClass(
        AdminUpdateUserAttributesRequest.class);
    verify(cognitoClient).adminUpdateUserAttributes(requestCaptor.capture());

    AdminUpdateUserAttributesRequest request = requestCaptor.getValue();
    assertThat("Unexpected user pool id.", request.userPoolId(), is(USER_POOL_ID));
    assertThat("Unexpected username.", request.username(), is(USERNAME));

    List<AttributeType> attributes = request.userAttributes();
    assertThat("Unexpected attribute count.", attributes.size(), is(3));

    Map<String, String> attributeMap = attributes.stream()
        .collect(Collectors.toMap(AttributeType::name, AttributeType::value));
    assertThat("Unexpected email.", attributeMap.get(EMAIL_FIELD), is(EMAIL_VALUE));
    assertThat("Unexpected given name.", attributeMap.get(GIVEN_NAME_FIELD), is(GIVEN_NAME_VALUE));
    assertThat("Unexpected family name.", attributeMap.get(FAMILY_NAME_FIELD),
        is(FAMILY_NAME_VALUE));
  }

  @Test
  void shouldReturnTrueWhenUserUpdatedByUserDto() {
    boolean updated = service.updateUser(new UserDTO());

    assertThat("Unexpected updated state.", updated, is(true));
  }

  @Test
  void shouldReturnFalseWhenUserUpdateFailsByUserDto() {
    when(cognitoClient.adminUpdateUserAttributes(any(AdminUpdateUserAttributesRequest.class)))
        .thenThrow(CognitoIdentityProviderException.builder().message("Dummy Exception.").build());

    boolean updated = service.updateUser(new UserDTO());

    assertThat("Unexpected updated state.", updated, is(false));
  }

  @Test
  void shouldSendDeleteCognitoUserRequest() {
    AuthenticationUserDto authenticationUser = new AuthenticationUserDto();
    authenticationUser.setUsername(USERNAME);

    DeleteAuthenticationUserRequestedEvent event = new DeleteAuthenticationUserRequestedEvent(
        authenticationUser, false);
    service.deleteKeycloakUserEventListener(event);

    ArgumentCaptor<AdminDeleteUserRequest> requestCaptor = ArgumentCaptor.forClass(
        AdminDeleteUserRequest.class);
    verify(cognitoClient).adminDeleteUser(requestCaptor.capture());

    AdminDeleteUserRequest request = requestCaptor.getValue();
    assertThat("Unexpected user pool id.", request.userPoolId(), is(USER_POOL_ID));
    assertThat("Unexpected username.", request.username(), is(USERNAME));
  }

  @Test
  void shouldPublishDeleteProfileUserEventWhenPublishIsTrue() {
    AuthenticationUserDto authenticationUser = new AuthenticationUserDto();
    authenticationUser.setUsername(USERNAME);

    DeleteAuthenticationUserRequestedEvent event = new DeleteAuthenticationUserRequestedEvent(
        authenticationUser, true);
    service.deleteKeycloakUserEventListener(event);

    ArgumentCaptor<DeleteProfileUserRequestEvent> eventCaptor = ArgumentCaptor.forClass(
        DeleteProfileUserRequestEvent.class);
    verify(eventPublisher).publishEvent(eventCaptor.capture());

    assertThat("Unexpected username.", eventCaptor.getValue().getUsername(), is(USERNAME));
  }

  @Test
  void shouldNotPublishDeleteProfileUserEventWhenPublishIsFalse() {
    AuthenticationUserDto authenticationUser = new AuthenticationUserDto();
    authenticationUser.setUsername(USERNAME);

    DeleteAuthenticationUserRequestedEvent event = new DeleteAuthenticationUserRequestedEvent(
        authenticationUser, false);
    service.deleteKeycloakUserEventListener(event);

    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  void shouldGetAuthEventsForUser() {
    Instant startTime = Instant.now();

    List<AuthEventType> events = createAuthEventsList(startTime);
    AdminListUserAuthEventsResponse requestResult = AdminListUserAuthEventsResponse.builder()
        .authEvents(events)
        .build();
    AdminListUserAuthEventsRequest request = AdminListUserAuthEventsRequest.builder()
        .userPoolId(USER_POOL_ID)
        .username(USERNAME)
        .maxResults(MAX_AUTH_EVENTS)
        .build();

    when(cognitoClient.adminListUserAuthEvents(request)).thenReturn(requestResult);

    List<UserAuthEventDto> results = service.getUserAuthEvents(USERNAME);

    assertThat(results.size(), is(MAX_AUTH_EVENTS));

    for (int i = 0; i < results.size(); i++) {
      UserAuthEventDto result = results.get(i);
      assertThat(result.getEventId(), is(String.valueOf(i)));
      assertThat(result.getEventDate(),
          is(Date.from(startTime.plusSeconds(i))));
      assertThat(result.getEvent(), is("SignIn"));
      assertThat(result.getResult(), is("Pass"));
      assertThat(result.getChallenges(), is("Password:Success, Mfa:Success"));
      assertThat(result.getDevice(), is("Chrome 126, Windows 10"));
    }
  }

  @Test
  void shouldUpdatePassword() {
    service.updatePassword(USERNAME, PASSWORD, true);

    ArgumentCaptor<AdminSetUserPasswordRequest> requestCaptor = ArgumentCaptor.forClass(
        AdminSetUserPasswordRequest.class);
    verify(cognitoClient).adminSetUserPassword(requestCaptor.capture());
    AdminSetUserPasswordRequest request = requestCaptor.getValue();
    Optional<String> optionalUserPoolId = request.getValueForField("UserPoolId", String.class);
    Optional<String> optionalUsername = request.getValueForField("Username", String.class);
    Optional<String> optionalPassword = request.getValueForField("Password", String.class);
    Optional<Boolean> optionalPermanent = request.getValueForField("Permanent", Boolean.class);
    assertTrue(optionalUsername.isPresent());
    assertTrue(optionalUserPoolId.isPresent());
    assertTrue(optionalPassword.isPresent());
    assertTrue(optionalPermanent.isPresent());
    assertEquals(USER_POOL_ID, optionalUserPoolId.get());
    assertEquals(USERNAME, optionalUsername.get());
    assertEquals(PASSWORD, optionalPassword.get());
    assertFalse(optionalPermanent.get());
    verify(emailService).sendTemporaryPasswordEmail(USERNAME, PASSWORD);
  }

  @Test
  void shouldThrowIdentityProviderExceptionIn() {

    AdminListUserAuthEventsRequest request = AdminListUserAuthEventsRequest.builder()
        .userPoolId(USER_POOL_ID)
        .username(USERNAME)
        .maxResults(MAX_AUTH_EVENTS)
        .build();

    // e.g. User Not Found
    when(cognitoClient.adminListUserAuthEvents(request)).thenThrow(UserNotFoundException.class);

    assertThrows(CognitoIdentityProviderException.class,
        () -> service.getUserAuthEvents(USERNAME));
  }

  /**
   * Build a list of standard Cognito attributes.
   *
   * @return The built list.
   */
  private List<AttributeType> buildStandardCognitoAttributes() {
    return Arrays.asList(
        AttributeType.builder().name(SUB_FIELD).value(SUB_VALUE).build(),
        AttributeType.builder().name(GIVEN_NAME_FIELD).value(GIVEN_NAME_VALUE).build(),
        AttributeType.builder().name(FAMILY_NAME_FIELD).value(FAMILY_NAME_VALUE).build(),
        AttributeType.builder().name(EMAIL_FIELD).value(EMAIL_VALUE).build()
    );
  }

  /**
   * Verify that the authentication user has the expected values.
   *
   * @param authenticationUser The authentication user to verify.
   */
  private void verifyAuthenticationUser(AuthenticationUserDto authenticationUser) {
    assertThat("Unexpected user id.", authenticationUser.getId(), is(SUB_VALUE));
    assertThat("Unexpected username.", authenticationUser.getUsername(), is(EMAIL_VALUE));
    assertThat("Unexpected given name.", authenticationUser.getGivenName(), is(GIVEN_NAME_VALUE));
    assertThat("Unexpected family name.", authenticationUser.getFamilyName(),
        is(FAMILY_NAME_VALUE));
    assertThat("Unexpected email.", authenticationUser.getEmail(), is(EMAIL_VALUE));
    assertThat("Unexpected enabled state.", authenticationUser.isEnabled(), is(true));

    Map<String, List<String>> attributes = authenticationUser.getAttributes();
    assertThat("Unexpected attribute count.", attributes.size(), is(4));

    assertThat("Unexpected sub.", attributes.get(SUB_FIELD), is(singletonList(SUB_VALUE)));
    assertThat("Unexpected email.", attributes.get(EMAIL_FIELD), is(singletonList(EMAIL_VALUE)));
    assertThat("Unexpected given name.", attributes.get(GIVEN_NAME_FIELD),
        is(singletonList(GIVEN_NAME_VALUE)));
    assertThat("Unexpected family name.", attributes.get(FAMILY_NAME_FIELD),
        is(singletonList(FAMILY_NAME_VALUE)));
  }

  private List<AuthEventType> createAuthEventsList(Instant startTime) {
    ChallengeResponseType challengeResponseType1 = ChallengeResponseType.builder()
        .challengeName("Password")
        .challengeResponse("Success")
        .build();
    ChallengeResponseType challengeResponseType2 = ChallengeResponseType.builder()
        .challengeName("Mfa")
        .challengeResponse("Success")
        .build();
    EventContextDataType eventContextDataType = EventContextDataType.builder()
        .deviceName("Chrome 126, Windows 10")
        .build();

    return IntStream.range(0, MAX_AUTH_EVENTS)
        .mapToObj(n -> AuthEventType.builder()
            .eventId(String.valueOf(n))
            .eventType("SignIn")
            .creationDate(Date.from(startTime.plusSeconds(n)).toInstant())
            .eventResponse("Pass")
            .challengeResponses(List.of(challengeResponseType1, challengeResponseType2))
            .eventContextData(eventContextDataType)
            .build()
        )
        .collect(Collectors.toList());
  }
}
