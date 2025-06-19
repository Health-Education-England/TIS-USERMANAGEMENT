package uk.nhs.hee.tis.usermanagement.service;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.nhs.hee.tis.usermanagement.service.CognitoAuthenticationAdminService.*;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClient;
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
import com.amazonaws.services.cognitoidp.model.AuthEventType;
import com.amazonaws.services.cognitoidp.model.ChallengeResponseType;
import com.amazonaws.services.cognitoidp.model.EventContextDataType;
import com.amazonaws.services.cognitoidp.model.InvalidParameterException;
import com.amazonaws.services.cognitoidp.model.ListUsersRequest;
import com.amazonaws.services.cognitoidp.model.ListUsersResult;
import com.amazonaws.services.cognitoidp.model.UserType;
import com.transformuk.hee.tis.profile.service.dto.HeeUserDTO;
import java.time.Instant;
import java.util.Arrays;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import uk.nhs.hee.tis.usermanagement.DTOs.UserAuthEventDto;
import uk.nhs.hee.tis.usermanagement.DTOs.AuthenticationUserDto;
import uk.nhs.hee.tis.usermanagement.DTOs.CreateUserDTO;
import uk.nhs.hee.tis.usermanagement.DTOs.UserDTO;
import uk.nhs.hee.tis.usermanagement.event.CreateAuthenticationUserRequestedEvent;
import uk.nhs.hee.tis.usermanagement.event.CreateProfileUserRequestedEvent;
import uk.nhs.hee.tis.usermanagement.event.DeleteAuthenticationUserRequestedEvent;
import uk.nhs.hee.tis.usermanagement.event.DeleteProfileUserRequestEvent;
import uk.nhs.hee.tis.usermanagement.mapper.AuthenticationUserMapperImpl;
import uk.nhs.hee.tis.usermanagement.mapper.CognitoRequestMapperImpl;
import uk.nhs.hee.tis.usermanagement.mapper.CognitoResultMapperImpl;

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

  private AWSCognitoIdentityProviderClient cognitoClient;
  private ApplicationEventPublisher eventPublisher;

  private static Stream<Exception> catchableExceptionProvider() {
    return Stream.of(new InvalidParameterException("Limited characters are permitted"));
  }

  @BeforeEach
  void setUp() {
    cognitoClient = mock(AWSCognitoIdentityProviderClient.class);
    eventPublisher = mock(ApplicationEventPublisher.class);
    service = new CognitoAuthenticationAdminService(
        eventPublisher,
        cognitoClient,
        USER_POOL_ID,
        new CognitoRequestMapperImpl(),
        new CognitoResultMapperImpl(),
        new AuthenticationUserMapperImpl()
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
    assertThat("Unexpected user pool id.", request.getUserPoolId(), is(USER_POOL_ID));
    assertThat("Unexpected username.", request.getUsername(), is(USERNAME));
    assertThat("Unexpected password.", request.getTemporaryPassword(), nullValue());

    List<AttributeType> attributes = request.getUserAttributes();
    assertThat("Unexpected attribute count.", attributes.size(), is(4));

    Map<String, String> attributeMap = attributes.stream()
        .collect(Collectors.toMap(AttributeType::getName, AttributeType::getValue));
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

    AdminCreateUserResult result = new AdminCreateUserResult()
        .withUser(new UserType()
            .withUsername(USERNAME)
            .withAttributes(Collections.emptyList()));
    when(cognitoClient.adminCreateUser(any())).thenReturn(result);

    service.createUser(dto);

    verify(cognitoClient, never()).adminDisableUser(any());
  }

  @Test
  void shouldDisableCreatedUserWhenActiveFalse() {
    CreateUserDTO dto = new CreateUserDTO();
    dto.setActive(false);

    AdminCreateUserResult result = new AdminCreateUserResult()
        .withUser(new UserType()
            .withUsername(USERNAME)
            .withAttributes(Collections.emptyList()));
    when(cognitoClient.adminCreateUser(any())).thenReturn(result);

    service.createUser(dto);

    ArgumentCaptor<AdminDisableUserRequest> requestCaptor = ArgumentCaptor.forClass(
        AdminDisableUserRequest.class);
    verify(cognitoClient).adminDisableUser(requestCaptor.capture());

    AdminDisableUserRequest request = requestCaptor.getValue();
    assertThat("Unexpected user pool id.", request.getUserPoolId(), is(USER_POOL_ID));
    assertThat("Unexpected username.", request.getUsername(), is(USERNAME));
  }

  @Test
  void shouldPublishCreatedUserWhenUserCreated() {
    UserType cognitoUser = new UserType();
    cognitoUser.setUsername(USERNAME);
    cognitoUser.setAttributes(buildStandardCognitoAttributes());
    cognitoUser.setEnabled(true);

    AdminCreateUserResult result = new AdminCreateUserResult();
    result.setUser(cognitoUser);

    when(cognitoClient.adminCreateUser(any())).thenReturn(result);

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
    ListUsersResult result = new ListUsersResult();
    result.setUsers(Collections.emptyList());

    ArgumentCaptor<ListUsersRequest> requestCaptor = ArgumentCaptor.forClass(
        ListUsersRequest.class);
    when(cognitoClient.listUsers(requestCaptor.capture())).thenReturn(result);

    service.getUser(USERNAME);

    ListUsersRequest request = requestCaptor.getValue();
    assertThat("Unexpected user pool id.", request.getUserPoolId(), is(USER_POOL_ID));
    assertThat("Unexpected username.", request.getFilter(), containsString(USERNAME));
  }

  @Test
  void shouldReturnGetCognitoUserResultWhenUsernameFound() {
    ListUsersResult result = new ListUsersResult();
    UserType user = new UserType();
    user.setEnabled(true);
    user.setUsername(USERNAME);
    user.setAttributes(buildStandardCognitoAttributes());
    result.setUsers(Collections.singletonList(user));

    when(cognitoClient.listUsers(any())).thenReturn(result);

    Optional<AuthenticationUserDto> optionalAuthenticationUser = service.getUser(USERNAME);

    assertThat("Expected user not found.", optionalAuthenticationUser.isPresent(), is(true));
    verifyAuthenticationUser(optionalAuthenticationUser.get());
  }

  @ParameterizedTest
  @MethodSource("catchableExceptionProvider")
  void shouldReturnEmptyGetCognitoUserResultWhenUsernameNotFound(Exception e) {
    when(cognitoClient.listUsers(any())).thenThrow(e);

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
    assertThat("Unexpected user pool id.", request.getUserPoolId(), is(USER_POOL_ID));
    assertThat("Unexpected username.", request.getUsername(), is(USERNAME));

    List<AttributeType> attributes = request.getUserAttributes();
    assertThat("Unexpected attribute count.", attributes.size(), is(3));

    Map<String, String> attributeMap = attributes.stream()
        .collect(Collectors.toMap(AttributeType::getName, AttributeType::getValue));
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
    assertThat("Unexpected user pool id.", request.getUserPoolId(), is(USER_POOL_ID));
    assertThat("Unexpected username.", request.getUsername(), is(USERNAME));
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
    assertThat("Unexpected user pool id.", request.getUserPoolId(), is(USER_POOL_ID));
    assertThat("Unexpected username.", request.getUsername(), is(USERNAME));
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

    List<AttributeType> attributes = requestCaptor.getValue().getUserAttributes();
    assertThat("Unexpected attribute count.", attributes.size(), is(3));

    Map<String, String> attributeMap = attributes.stream()
        .collect(Collectors.toMap(AttributeType::getName, AttributeType::getValue));
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
    when(cognitoClient.adminUpdateUserAttributes(any())).thenThrow(
        new AWSCognitoIdentityProviderException("Dummy Exception."));

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
    assertThat("Unexpected user pool id.", request.getUserPoolId(), is(USER_POOL_ID));
    assertThat("Unexpected username.", request.getUsername(), is(USERNAME));

    List<AttributeType> attributes = request.getUserAttributes();
    assertThat("Unexpected attribute count.", attributes.size(), is(3));

    Map<String, String> attributeMap = attributes.stream()
        .collect(Collectors.toMap(AttributeType::getName, AttributeType::getValue));
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
    when(cognitoClient.adminUpdateUserAttributes(any())).thenThrow(
        new AWSCognitoIdentityProviderException("Dummy Exception."));

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
    assertThat("Unexpected user pool id.", request.getUserPoolId(), is(USER_POOL_ID));
    assertThat("Unexpected username.", request.getUsername(), is(USERNAME));
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
    AdminListUserAuthEventsResult requestResult = new AdminListUserAuthEventsResult()
        .withAuthEvents(events);
    AdminListUserAuthEventsRequest request = new AdminListUserAuthEventsRequest()
        .withUserPoolId(USER_POOL_ID)
        .withUsername(USERNAME)
        .withMaxResults(MAX_AUTH_EVENTS);

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
  void shouldThrowIdentityProviderExceptionIn() {

    AdminListUserAuthEventsRequest request = new AdminListUserAuthEventsRequest()
        .withUserPoolId(USER_POOL_ID)
        .withUsername(USERNAME)
        .withMaxResults(MAX_AUTH_EVENTS);

    // e.g. User Not Found
    when(cognitoClient.adminListUserAuthEvents(request)).thenThrow(UserNotFoundException.class);

    assertThrows(AWSCognitoIdentityProviderException.class,
        () -> service.getUserAuthEvents(USERNAME));
  }

  /**
   * Build a list of standard Cognito attributes.
   *
   * @return The built list.
   */
  private List<AttributeType> buildStandardCognitoAttributes() {
    return Arrays.asList(
        new AttributeType().withName(SUB_FIELD).withValue(SUB_VALUE),
        new AttributeType().withName(GIVEN_NAME_FIELD).withValue(GIVEN_NAME_VALUE),
        new AttributeType().withName(FAMILY_NAME_FIELD).withValue(FAMILY_NAME_VALUE),
        new AttributeType().withName(EMAIL_FIELD).withValue(EMAIL_VALUE)
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
    ChallengeResponseType challengeResponseType1 = new ChallengeResponseType().withChallengeName(
        "Password").withChallengeResponse("Success");
    ChallengeResponseType challengeResponseType2 = new ChallengeResponseType().withChallengeName(
        "Mfa").withChallengeResponse("Success");
    EventContextDataType eventContextDataType = new EventContextDataType().withDeviceName(
        "Chrome 126, Windows 10");

    return IntStream.range(0, MAX_AUTH_EVENTS)
        .mapToObj(n -> new AuthEventType().withEventId(String.valueOf(n)).withEventType("SignIn")
            .withCreationDate(Date.from(startTime.plusSeconds(n))).withEventResponse("Pass")
            .withChallengeResponses(List.of(challengeResponseType1, challengeResponseType2))
            .withEventContextData(eventContextDataType))
        .collect(Collectors.toList());
  }
}
