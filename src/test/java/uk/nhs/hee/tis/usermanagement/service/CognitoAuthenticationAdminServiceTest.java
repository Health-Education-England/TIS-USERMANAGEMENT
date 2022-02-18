package uk.nhs.hee.tis.usermanagement.service;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClient;
import com.amazonaws.services.cognitoidp.model.AdminCreateUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminCreateUserResult;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.amazonaws.services.cognitoidp.model.UserType;
import com.transformuk.hee.tis.profile.service.dto.HeeUserDTO;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import uk.nhs.hee.tis.usermanagement.DTOs.AuthenticationUserDto;
import uk.nhs.hee.tis.usermanagement.DTOs.CreateUserDTO;
import uk.nhs.hee.tis.usermanagement.event.CreateAuthenticationUserRequestedEvent;
import uk.nhs.hee.tis.usermanagement.event.CreateProfileUserRequestedEvent;
import uk.nhs.hee.tis.usermanagement.mapper.CognitoRequestMapperImpl;
import uk.nhs.hee.tis.usermanagement.mapper.CognitoResultMapperImpl;

public class CognitoAuthenticationAdminServiceTest {

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

  @Before
  public void setUp() {
    cognitoClient = mock(AWSCognitoIdentityProviderClient.class);
    eventPublisher = mock(ApplicationEventPublisher.class);
    service = new CognitoAuthenticationAdminService(
        eventPublisher,
        cognitoClient,
        USER_POOL_ID,
        new CognitoRequestMapperImpl(),
        new CognitoResultMapperImpl()
    );
  }

  @Test
  public void shouldSendCreateCognitoUserRequestWhenCreateEventReceived() {
    CreateUserDTO dto = new CreateUserDTO();
    dto.setName(USERNAME);
    dto.setEmailAddress(EMAIL_VALUE);
    dto.setPassword(PASSWORD);
    dto.setFirstName(GIVEN_NAME_VALUE);
    dto.setLastName(FAMILY_NAME_VALUE);

    CreateAuthenticationUserRequestedEvent event = new CreateAuthenticationUserRequestedEvent(dto,
        new HeeUserDTO());
    service.createUserEventListener(event);

    ArgumentCaptor<AdminCreateUserRequest> requestCaptor = ArgumentCaptor.forClass(
        AdminCreateUserRequest.class);
    verify(cognitoClient).adminCreateUser(requestCaptor.capture());

    AdminCreateUserRequest request = requestCaptor.getValue();
    assertThat("Unexpected user pool id.", request.getUserPoolId(), is(USER_POOL_ID));
    assertThat("Unexpected username.", request.getUsername(), is(USERNAME));
    assertThat("Unexpected password.", request.getTemporaryPassword(), is(PASSWORD));

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
  public void shouldPublishCreateCognitoUserResultWhenCreateEventReceived() {
    UserType cognitoUser = new UserType();
    cognitoUser.setUsername(USERNAME);
    cognitoUser.setAttributes(Arrays.asList(
        new AttributeType().withName(SUB_FIELD).withValue(SUB_VALUE),
        new AttributeType().withName(GIVEN_NAME_FIELD).withValue(GIVEN_NAME_VALUE),
        new AttributeType().withName(FAMILY_NAME_FIELD).withValue(FAMILY_NAME_VALUE),
        new AttributeType().withName(EMAIL_FIELD).withValue(EMAIL_VALUE)
    ));
    cognitoUser.setEnabled(true);

    AdminCreateUserResult result = new AdminCreateUserResult();
    result.setUser(cognitoUser);

    when(cognitoClient.adminCreateUser(any())).thenReturn(result);

    CreateAuthenticationUserRequestedEvent event = new CreateAuthenticationUserRequestedEvent(
        new CreateUserDTO(), new HeeUserDTO());
    service.createUserEventListener(event);

    ArgumentCaptor<CreateProfileUserRequestedEvent> eventCaptor = ArgumentCaptor.forClass(
        CreateProfileUserRequestedEvent.class);
    verify(eventPublisher).publishEvent(eventCaptor.capture());

    AuthenticationUserDto authenticationUser = eventCaptor.getValue().getAuthenticationUser();

    assertThat("Unexpected user id.", authenticationUser.getId(), is(SUB_VALUE));
    assertThat("Unexpected username.", authenticationUser.getUsername(), is(USERNAME));
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

  @Test
  public void shouldPublishHeeUserToBeCreatedWhenCreateEventReceived() {
    HeeUserDTO heeUser = new HeeUserDTO();

    CreateAuthenticationUserRequestedEvent event = new CreateAuthenticationUserRequestedEvent(
        new CreateUserDTO(), heeUser);
    service.createUserEventListener(event);

    ArgumentCaptor<CreateProfileUserRequestedEvent> eventCaptor = ArgumentCaptor.forClass(
        CreateProfileUserRequestedEvent.class);
    verify(eventPublisher).publishEvent(eventCaptor.capture());

    assertThat("Unexpected hee user.", eventCaptor.getValue().getHeeUserDTO(),
        sameInstance(heeUser));
  }
}
