package uk.nhs.hee.tis.usermanagement.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.nhs.hee.tis.usermanagement.service.KeyCloakAdminClientService.REALM_LIN;

import com.google.common.collect.Maps;
import com.transform.hee.tis.keycloak.KeycloakAdminClient;
import com.transform.hee.tis.keycloak.User;
import com.transformuk.hee.tis.profile.service.dto.HeeUserDTO;
import java.util.List;
import java.util.Map;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.keycloak.representations.idm.GroupRepresentation;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;
import uk.nhs.hee.tis.usermanagement.DTOs.CreateUserDTO;
import uk.nhs.hee.tis.usermanagement.DTOs.UserDTO;
import uk.nhs.hee.tis.usermanagement.event.CreateKeycloakUserRequestedEvent;
import uk.nhs.hee.tis.usermanagement.event.DeleteKeycloakUserRequestedEvent;
import uk.nhs.hee.tis.usermanagement.event.DeleteProfileUserRequestEvent;
import uk.nhs.hee.tis.usermanagement.exception.PasswordException;
import uk.nhs.hee.tis.usermanagement.exception.UserNotFoundException;

@RunWith(MockitoJUnitRunner.class)
public class KeyCloakAdminClientServiceTest {

  private static final String FIRST_NAME = "FIRST NAME";
  private static final String LAST_NAME = "LAST NAME";
  private static final String NAME = "NAME";
  private static final String EMAIL_ADDRESS = "EMAIL ADDRESS";
  private static final String PASSWORD = "PASSWORD";
  private static final boolean TEMPORARY_PASSWORD = true;
  private static final boolean ACTIVE = false;
  private static final String NAME_THAT_IS_NOT_IN_THE_SYSTEM = "NAME THAT IS NOT IN THE SYSTEM";
  private static final String USER_ID = "USER ID";

  @InjectMocks
  private KeyCloakAdminClientService testObj;

  @Mock
  private KeycloakAdminClient keycloakAdminClientMock;

  @Captor
  private ArgumentCaptor<User> userArgumentCaptor;

  @Mock
  private ApplicationEventPublisher applicationEventPublisherMock;


  private UserDTO createUserDTO() {
    UserDTO userDTO = new UserDTO();
    userDTO.setKcId(USER_ID);
    userDTO.setFirstName(FIRST_NAME);
    userDTO.setLastName(LAST_NAME);
    userDTO.setName(NAME);
    userDTO.setEmailAddress(EMAIL_ADDRESS);
    userDTO.setActive(ACTIVE);
    return userDTO;
  }

  private CreateUserDTO createCreateUserDTO() {
    CreateUserDTO createUserDTO = new CreateUserDTO();
    createUserDTO.setFirstName(FIRST_NAME);
    createUserDTO.setLastName(LAST_NAME);
    createUserDTO.setName(NAME);
    createUserDTO.setEmailAddress(EMAIL_ADDRESS);
    createUserDTO.setPassword(PASSWORD);
    createUserDTO.setConfirmPassword(PASSWORD);
    createUserDTO.setTempPassword(TEMPORARY_PASSWORD);
    createUserDTO.setActive(ACTIVE);
    return createUserDTO;
  }

  @Test(expected = NullPointerException.class)
  public void createUserEventListenerShouldReturnExceptionWhenUserIsNull() {
    try {
      testObj.createUserEventListener(null);
    } finally {
      verifyZeroInteractions(keycloakAdminClientMock);
    }
  }

  @Test
  public void createUserEventListenerShouldCreateUserUsingKeyCloakAdminClient() {
    CreateUserDTO createUserDTO = createCreateUserDTO();
    User expectedCreatedUser = User.create("ID", FIRST_NAME, LAST_NAME, NAME, EMAIL_ADDRESS,
        PASSWORD, TEMPORARY_PASSWORD, Maps.newHashMap(), ACTIVE);

    when(
        keycloakAdminClientMock.createUser(eq(REALM_LIN), userArgumentCaptor.capture())).thenReturn(
        expectedCreatedUser);

    testObj.createUserEventListener(
        new CreateKeycloakUserRequestedEvent(createUserDTO, new HeeUserDTO()));

    User capturedUser = userArgumentCaptor.getValue();
    Assert.assertEquals(FIRST_NAME, capturedUser.getFirstname());
    Assert.assertEquals(LAST_NAME, capturedUser.getSurname());
    Assert.assertEquals(NAME, capturedUser.getUsername());
    Assert.assertEquals(EMAIL_ADDRESS, capturedUser.getEmail());
    Assert.assertEquals(PASSWORD, capturedUser.getPassword());
    Assert.assertEquals(TEMPORARY_PASSWORD, capturedUser.getTempPassword());
    Assert.assertEquals(ACTIVE, capturedUser.getEnabled());

    verify(keycloakAdminClientMock).createUser(REALM_LIN, capturedUser);
  }

  @Test(expected = NullPointerException.class)
  public void updateUserShouldThrowExceptionWhenUserIsNull() {
    try {
      testObj.updateUser((UserDTO) null);
    } finally {
      verifyZeroInteractions(keycloakAdminClientMock);
    }
  }

  @Test(expected = UserNotFoundException.class)
  public void updateUserShouldThrowExceptionWhenUserCannotBeFound() {
    UserDTO userDTO = new UserDTO();
    userDTO.setName(NAME_THAT_IS_NOT_IN_THE_SYSTEM);

    try {
      testObj.updateUser(userDTO);
    } catch (UserNotFoundException unfe) {
      Assert.assertEquals(
          "Could not find user: [" + NAME_THAT_IS_NOT_IN_THE_SYSTEM + "] in the keycloak service",
          unfe.getMessage());
      throw unfe;
    } finally {
      verify(keycloakAdminClientMock).findByUsername(REALM_LIN, NAME_THAT_IS_NOT_IN_THE_SYSTEM);
      verify(keycloakAdminClientMock, never()).updateUser(anyString(), anyString(),
          any(User.class));
    }
  }

  @Test
  public void updateUserShouldUpdateUserUsingProvidedDetails() {
    UserDTO userDTO = createUserDTO();
    User userMock = mock(User.class);

    when(keycloakAdminClientMock.findByUsername(REALM_LIN, NAME)).thenReturn(userMock);

    testObj.updateUser(userDTO);

    verify(keycloakAdminClientMock).updateUser(eq(REALM_LIN), eq(USER_ID),
        userArgumentCaptor.capture());

    User capturedUser = userArgumentCaptor.getValue();
    Assert.assertEquals(FIRST_NAME, capturedUser.getFirstname());
    Assert.assertEquals(LAST_NAME, capturedUser.getSurname());
    Assert.assertEquals(NAME, capturedUser.getUsername());
    Assert.assertEquals(EMAIL_ADDRESS, capturedUser.getEmail());
    Assert.assertEquals(Lists.emptyList(), capturedUser.getAttributes().get("DBC"));
    Assert.assertEquals(ACTIVE, capturedUser.getEnabled());
  }

  @Test(expected = NullPointerException.class)
  public void getUserAttributesShouldThrowExceptionWhenUsernameIsNull() {
    try {
      testObj.getUserAttributes(null);
    } finally {
      verifyZeroInteractions(keycloakAdminClientMock);
    }
  }

  @Test
  public void getUserAttributesShouldCallKeyCloakToGetUserAttributes() {
    Map<String, List<String>> userAttributes = Maps.newHashMap();
    userAttributes.put("A KEY", Lists.newArrayList("A VALUE"));
    when(keycloakAdminClientMock.getAttributesForUser(REALM_LIN, NAME)).thenReturn(userAttributes);

    Map<String, List<String>> result = testObj.getUserAttributes(NAME);

    Assert.assertSame(userAttributes, result);
  }

  @Test(expected = NullPointerException.class)
  public void getUserGroupsShouldThrowExceptionWhenUsernameIsNull() {
    try {
      testObj.getUserGroups(null);
    } finally {
      verifyZeroInteractions(keycloakAdminClientMock);
    }
  }

  @Test(expected = UserNotFoundException.class)
  public void getUserGroupsShouldThrowUserNotFoundExceptionWhenUserCannotBeFoundInKC() {
    when(keycloakAdminClientMock.findByUsername(REALM_LIN, NAME)).thenReturn(null);
    try {
      testObj.getUserGroups(NAME);
    } finally {
      verify(keycloakAdminClientMock, never()).listGroups(anyString(), any(User.class));
    }
  }

  @Test
  public void getUserGroupsShouldReturnGroupsForKeyCloakUser() {
    User userMock = mock(User.class);
    GroupRepresentation groupRepresentation = new GroupRepresentation();
    groupRepresentation.setId("GROUP ID");
    List<GroupRepresentation> foundGroupRepresentations = Lists.newArrayList(groupRepresentation);

    when(keycloakAdminClientMock.findByUsername(REALM_LIN, NAME)).thenReturn(userMock);
    when(keycloakAdminClientMock.listGroups(REALM_LIN, userMock)).thenReturn(
        foundGroupRepresentations);

    List<GroupRepresentation> result = testObj.getUserGroups(NAME);

    Assert.assertSame(foundGroupRepresentations, result);

    verify(keycloakAdminClientMock).findByUsername(REALM_LIN, NAME);
    verify(keycloakAdminClientMock).listGroups(REALM_LIN, userMock);
  }

  @Test(expected = NullPointerException.class)
  public void updatePasswordShouldThrowExceptionWhenUserIdIsNull() {
    try {
      testObj.updatePassword(null, PASSWORD, TEMPORARY_PASSWORD);
    } finally {
      verify(keycloakAdminClientMock, never()).updateUserPassword(anyString(), anyString(),
          anyString(), anyBoolean());
    }
  }


  @Test(expected = NullPointerException.class)
  public void updatePasswordShouldThrowExceptionWhenPasswordIsNull() {
    try {
      testObj.updatePassword(USER_ID, null, TEMPORARY_PASSWORD);
    } finally {
      verify(keycloakAdminClientMock, never()).updateUserPassword(anyString(), anyString(),
          anyString(), anyBoolean());
    }
  }

  @Test(expected = PasswordException.class)
  public void updatePasswordShouldThrowPasswordExceptionWhenUpdateFails() {
    try {
      testObj.updatePassword(USER_ID, PASSWORD, TEMPORARY_PASSWORD);
    } finally {
      verify(keycloakAdminClientMock).updateUserPassword(REALM_LIN, USER_ID, PASSWORD,
          TEMPORARY_PASSWORD);
    }
  }

  @Test
  public void updatePasswordShouldReturnTrueWhenUpdateSucceedsWithKC() {
    when(keycloakAdminClientMock.updateUserPassword(REALM_LIN, USER_ID, PASSWORD,
        TEMPORARY_PASSWORD)).thenReturn(true);

    boolean result = testObj.updatePassword(USER_ID, PASSWORD, TEMPORARY_PASSWORD);
    Assert.assertTrue(result);

    verify(keycloakAdminClientMock).updateUserPassword(REALM_LIN, USER_ID, PASSWORD,
        TEMPORARY_PASSWORD);
  }

  @Test
  public void deleteKeycloakUserEventListenerShouldPublishProfileDeleteWhenFlagTrue() {
    User kcUser = User.create("userId1", "", "", "user1", "", "", false, null, true);

    DeleteKeycloakUserRequestedEvent event = new DeleteKeycloakUserRequestedEvent(kcUser, true);
    testObj.deleteKeycloakUserEventListener(event);

    verify(keycloakAdminClientMock).removeUser(REALM_LIN, kcUser);

    ArgumentCaptor<DeleteProfileUserRequestEvent> profileEventCaptor = ArgumentCaptor.forClass(
        DeleteProfileUserRequestEvent.class);
    verify(applicationEventPublisherMock).publishEvent(profileEventCaptor.capture());

    DeleteProfileUserRequestEvent profileEvent = profileEventCaptor.getValue();
    assertThat("Unexpected username.", profileEvent.getUsername(), is("user1"));
  }

  @Test
  public void deleteKeycloakUserEventListenerShouldNotPublishProfileDeleteWhenFlagFalse() {
    User kcUser = new User();

    DeleteKeycloakUserRequestedEvent event = new DeleteKeycloakUserRequestedEvent(kcUser, false);
    testObj.deleteKeycloakUserEventListener(event);

    verify(keycloakAdminClientMock).removeUser(REALM_LIN, kcUser);
    verify(applicationEventPublisherMock, never()).publishEvent(
        any(DeleteProfileUserRequestEvent.class));
  }
}
