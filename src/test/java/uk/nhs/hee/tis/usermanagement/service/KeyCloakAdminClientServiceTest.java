package uk.nhs.hee.tis.usermanagement.service;

import com.google.common.collect.Maps;
import com.transform.hee.tis.keycloak.KeycloakAdminClient;
import com.transform.hee.tis.keycloak.User;
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
import uk.nhs.hee.tis.usermanagement.DTOs.CreateUserDTO;
import uk.nhs.hee.tis.usermanagement.DTOs.UserDTO;
import uk.nhs.hee.tis.usermanagement.exception.UserNotFoundException;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.nhs.hee.tis.usermanagement.service.KeyCloakAdminClientService.REALM_LIN;

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
  public void createUserShouldReturnExceptionWhenUserIsNull() {
    try {
      testObj.createUser(null);
    } finally {
      verifyZeroInteractions(keycloakAdminClientMock);
    }
  }

  @Test
  public void createUserShouldCreateUserUsingKeyCloakAdminClient() {
    CreateUserDTO createUserDTO = createCreateUserDTO();
    User expectedCreatedUser = User.create("ID", FIRST_NAME, LAST_NAME, NAME, EMAIL_ADDRESS, PASSWORD, TEMPORARY_PASSWORD, Maps.newHashMap(), ACTIVE);

    when(keycloakAdminClientMock.createUser(eq(REALM_LIN), userArgumentCaptor.capture())).thenReturn(expectedCreatedUser);

    testObj.createUser(createUserDTO);

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
      Assert.assertEquals("Could not find user: [" + NAME_THAT_IS_NOT_IN_THE_SYSTEM + "] in the keycloak service", unfe.getMessage());
      throw unfe;
    } finally {
      verify(keycloakAdminClientMock).findByUsername(REALM_LIN, NAME_THAT_IS_NOT_IN_THE_SYSTEM);
      verify(keycloakAdminClientMock, never()).updateUser(anyString(), anyString(), any(User.class));
    }
  }

  @Test
  public void updateUserShouldUpdateUserUsingProvidedDetails() {
    UserDTO userDTO = createUserDTO();
    User userMock = mock(User.class);

    when(keycloakAdminClientMock.findByUsername(REALM_LIN, NAME)).thenReturn(userMock);

    testObj.updateUser(userDTO);

    verify(keycloakAdminClientMock).updateUser(eq(REALM_LIN), eq(USER_ID), userArgumentCaptor.capture());

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
    when(keycloakAdminClientMock.listGroups(REALM_LIN, userMock)).thenReturn(foundGroupRepresentations);

    List<GroupRepresentation> result = testObj.getUserGroups(NAME);

    Assert.assertSame(foundGroupRepresentations, result);

    verify(keycloakAdminClientMock).findByUsername(REALM_LIN, NAME);
    verify(keycloakAdminClientMock).listGroups(REALM_LIN, userMock);

  }
}