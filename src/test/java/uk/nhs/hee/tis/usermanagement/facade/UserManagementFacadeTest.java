package uk.nhs.hee.tis.usermanagement.facade;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import com.transform.hee.tis.keycloak.User;
import com.transformuk.hee.tis.profile.dto.RoleDTO;
import com.transformuk.hee.tis.profile.service.dto.HeeUserDTO;
import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeDTO;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import uk.nhs.hee.tis.usermanagement.DTOs.CreateUserDTO;
import uk.nhs.hee.tis.usermanagement.DTOs.UserDTO;
import uk.nhs.hee.tis.usermanagement.DTOs.UserPasswordDTO;
import uk.nhs.hee.tis.usermanagement.event.CreateKeycloakUserRequestedEvent;
import uk.nhs.hee.tis.usermanagement.event.DeleteKeycloakUserRequestedEvent;
import uk.nhs.hee.tis.usermanagement.exception.UpdateUserException;
import uk.nhs.hee.tis.usermanagement.exception.UserNotFoundException;
import uk.nhs.hee.tis.usermanagement.mapper.HeeUserMapper;
import uk.nhs.hee.tis.usermanagement.service.KeyCloakAdminClientService;
import uk.nhs.hee.tis.usermanagement.service.ProfileService;
import uk.nhs.hee.tis.usermanagement.service.ReferenceService;
import uk.nhs.hee.tis.usermanagement.service.TcsService;

@RunWith(MockitoJUnitRunner.class)
public class UserManagementFacadeTest {

  private final String adminRole = "HEE TIS Admin";
  private final String etlRole = "ETL";
  private final String roRole = "RVOfficer";
  private final String rvAdmin = "RVAdmin";
  private final String HEE = "HEE";

  @InjectMocks
  UserManagementFacade testClass;

  @Mock
  ProfileService profileService;

  @Mock
  ReferenceService referenceService;

  @Mock
  TcsService tcsService;

  @Mock
  KeyCloakAdminClientService keyCloakAdminClientService;

  @Mock
  ApplicationEventPublisher applicationEventPublisher;

  @Spy
  private HeeUserMapper userMapper;

  @Rule
  public ExpectedException exceptionRule = ExpectedException.none();

  @Test
  public void shouldGetAllAssignableRoles() {
    List<String> mockRoles = Lists.newArrayList(adminRole, rvAdmin, roRole, etlRole);

    when(profileService.getAllRoles()).thenReturn(mockRoles);
    List<String> actual = testClass.getAllAssignableRoles();
    assertThat(actual, containsInAnyOrder(adminRole, rvAdmin, etlRole));
    verify(profileService).getAllRoles();
  }

  @Test
  public void shouldGetAllEntityRoles() {
    List<String> entityRoles = testClass.getAllEntityRoles();
    assertThat(entityRoles, containsInAnyOrder(HEE));
  }

  @Test
  public void testGetNullUserByNameWhenUserNotFound() {
    // Record expectations.
    when(profileService.getUserByUsernameIgnoreCase("testUser_1")).thenReturn(Optional.empty());

    // Call the code under test.
    UserDTO user = testClass.getUserByNameIgnoreCase("testUser_1");

    // Perform assertions.
    assertThat("The user did not match the expected value.", user, nullValue());
  }

  @Test
  public void shouldGetUserByNameIgnoringCaseWhenUserFound() {
    // Set up test data.
    HeeUserDTO heeUser = new HeeUserDTO();
    heeUser.setName("testUser_1");

    // Record expectations.
    when(profileService.getUserByUsernameIgnoreCase("testUser_1")).thenReturn(Optional.of(heeUser));

    // Call the code under test.
    UserDTO user = testClass.getUserByNameIgnoreCase("testUser_1");

    // Perform assertions.
    assertThat("The user's name did not match the expected value.", user.getName(),
        is("testUser_1"));

    // Verify expectations.
    verify(userMapper).convert(heeUser);
  }

  @Test
  public void shouldThrowExceptionGettingCompleteUserWhenUserNotFoundInProfile() {
    when(keyCloakAdminClientService.getUser("user1")).thenReturn(Optional.of(new User()));

    exceptionRule.expect(UserNotFoundException.class);
    exceptionRule.expectMessage(containsString("user1"));
    exceptionRule.expectMessage(containsString(ProfileService.NAME));

    testClass.getCompleteUser("user1");
  }

  @Test
  public void shouldThrowExceptionGettingCompleteUserWhenUserNotFoundInKeycloak() {
    when(profileService.getUserByUsername("user1")).thenReturn(Optional.of(new HeeUserDTO()));

    exceptionRule.expect(UserNotFoundException.class);
    exceptionRule.expectMessage(containsString("user1"));
    exceptionRule.expectMessage(containsString(KeyCloakAdminClientService.NAME));

    testClass.getCompleteUser("user1");
  }

  @Test
  public void shouldGetCompleteUserWhenUserFound() {
    HeeUserDTO heeUser = new HeeUserDTO();
    heeUser.setName("user1");

    User kcUser = User.create("userId1", "", "", "", "", "", false, null, true);

    when(profileService.getUserByUsername("user1")).thenReturn(Optional.of(heeUser));
    when(keyCloakAdminClientService.getUser("user1")).thenReturn(Optional.of(kcUser));

    UserDTO user = testClass.getCompleteUser("user1");
    assertThat("Unexpected user id.", user.getKcId(), is("userId1"));
    assertThat("Unexpected user name.", user.getName(), is("user1"));
    assertThat("Unexpected user enabled flag.", user.getActive(), is(true));
  }

  @Test
  public void shouldGetAllUsers() {
    HeeUserDTO profileUser1 = new HeeUserDTO();
    profileUser1.setName("user1");
    HeeUserDTO profileUser2 = new HeeUserDTO();
    profileUser2.setName("user2");
    Page<HeeUserDTO> profileUsers = new PageImpl<>(Arrays.asList(profileUser1, profileUser2));

    when(profileService.getAllUsers(Pageable.unpaged(), "searchString")).thenReturn(profileUsers);

    Page<UserDTO> allUsersPage = testClass.getAllUsers(Pageable.unpaged(), "searchString");

    assertThat("Unexpected user count.", allUsersPage.getTotalElements(), is(2L));
    assertThat("Unexpected pageable.", allUsersPage.getPageable(), is(Pageable.unpaged()));

    Set<String> allUserNames = allUsersPage.stream()
        .map(UserDTO::getName)
        .collect(Collectors.toSet());
    assertThat("Unexpected user names.", allUserNames, hasItems("user1", "user2"));
  }

  @Test
  public void shouldThrowExceptionUpdatingSingleUserWhenUsernameNotFoundInKeycloak() {
    UserDTO user = new UserDTO();
    user.setName("user1");

    exceptionRule.expect(UserNotFoundException.class);
    exceptionRule.expectMessage(containsString("user1"));
    exceptionRule.expectMessage(containsString(KeyCloakAdminClientService.NAME));

    testClass.updateSingleUser(user);
  }

  @Test
  public void shouldThrowExceptionUpdatingSingleUserWhenKeycloakUpdateFails() {
    UserDTO user = new UserDTO();
    user.setName("user1");

    when(keyCloakAdminClientService.getUser("user1")).thenReturn(Optional.of(new User()));

    exceptionRule.expect(UpdateUserException.class);
    exceptionRule.expectMessage(containsString("user1"));
    exceptionRule.expectMessage(containsString(KeyCloakAdminClientService.NAME));

    testClass.updateSingleUser(user);
  }

  @Test
  public void shouldUpdateSingleKeycloakUserAndThrowExceptionWhenUsernameNotFoundInProfile() {
    UserDTO user = new UserDTO();
    user.setName("user1");

    when(keyCloakAdminClientService.getUser("user1")).thenReturn(Optional.of(new User()));
    when(keyCloakAdminClientService.updateUser(user)).thenReturn(true);

    exceptionRule.expect(UserNotFoundException.class);
    exceptionRule.expectMessage(containsString("user1"));
    exceptionRule.expectMessage(containsString(ProfileService.NAME));

    testClass.updateSingleUser(user);

    verify(keyCloakAdminClientService).updateUser(user);
  }

  @Test
  public void shouldNotUpdateSingleKeycloakUserAndThrowExceptionWhenProfileUpdateFails() {
    UserDTO user = new UserDTO();
    user.setName("user1");

    User kcUser = new User();
    kcUser.setId("userId1");

    HeeUserDTO heeUser = new HeeUserDTO();
    heeUser.setRoles(Collections.emptySet());

    when(keyCloakAdminClientService.getUser("user1")).thenReturn(Optional.of(kcUser));
    when(keyCloakAdminClientService.updateUser(user)).thenReturn(true);

    when(profileService.getUserByUsername("user1")).thenReturn(Optional.of(heeUser));

    exceptionRule.expect(UpdateUserException.class);
    exceptionRule.expectMessage(containsString("user1"));
    exceptionRule.expectMessage(containsString(ProfileService.NAME));

    testClass.updateSingleUser(user);

    // Verify the rollback.
    verify(keyCloakAdminClientService).updateUser(kcUser);
  }

  @Test
  public void shouldUpdateSingleUserWhenKeycloakAndProfileUpdatesSucceed() {
    UserDTO user = new UserDTO();
    user.setName("user1");
    Set<String> newRoles = new HashSet<>(Arrays.asList("role1", "role2"));
    user.setRoles(newRoles);

    User kcUser = new User();
    kcUser.setId("userId1");

    HeeUserDTO existingHeeUser = new HeeUserDTO();
    existingHeeUser.setRoles(buildRoleDtos("role1", "role3", "Machine User"));

    when(keyCloakAdminClientService.getUser("user1")).thenReturn(Optional.of(kcUser));
    when(keyCloakAdminClientService.updateUser(user)).thenReturn(true);

    when(profileService.getUserByUsername("user1")).thenReturn(Optional.of(existingHeeUser));
    ArgumentCaptor<HeeUserDTO> updatedHeeUserCaptor = ArgumentCaptor.forClass(HeeUserDTO.class);
    when(profileService.updateUser(updatedHeeUserCaptor.capture())).thenReturn(
        Optional.of(new HeeUserDTO()));

    testClass.updateSingleUser(user);

    HeeUserDTO updatedHeeUser = updatedHeeUserCaptor.getValue();
    assertThat("Unexpected user name.", updatedHeeUser.getName(), is("user1"));

    Set<String> updatedRoles = updatedHeeUser.getRoles().stream().map(RoleDTO::getName)
        .collect(Collectors.toSet());
    assertThat("Unexpected role count.", updatedRoles.size(), is(3));
    assertThat("Unexpected roles.", updatedRoles, hasItems("role1", "role2", "Machine User"));

    // Verify no rollback.
    verify(keyCloakAdminClientService, never()).updateUser(kcUser);
  }

  @Test
  public void shouldPublishUserCreationEvent() {
    CreateUserDTO createUser = new CreateUserDTO();
    createUser.setName("user1");
    createUser.setAssociatedProgrammes(new HashSet<>(Collections.singleton("1")));
    createUser.setAssociatedTrusts(new HashSet<>(Collections.singleton("2")));

    ProgrammeDTO programme = new ProgrammeDTO();
    programme.setId(1L);
    when(tcsService.getAllProgrammes()).thenReturn(Collections.singletonList(programme));

    TrustDTO trust = new TrustDTO();
    trust.setId(2L);
    when(referenceService.getAllTrusts()).thenReturn(Collections.singletonList(trust));

    testClass.publishUserCreationRequestedEvent(createUser);

    ArgumentCaptor<CreateKeycloakUserRequestedEvent> eventCaptor = ArgumentCaptor.forClass(
        CreateKeycloakUserRequestedEvent.class);
    verify(applicationEventPublisher).publishEvent(eventCaptor.capture());

    CreateKeycloakUserRequestedEvent event = eventCaptor.getValue();
    assertThat("Unexpected user.", event.getUserDTO(), is(createUser));

    HeeUserDTO profileUser = event.getUserToCreateInProfileService();
    assertThat("Unexpected user name.", profileUser.getName(), is("user1"));
    assertThat("Unexpected associated programme count.",
        profileUser.getAssociatedProgrammes().size(), is(1));
    assertThat("Unexpected associated programme id.",
        profileUser.getAssociatedProgrammes().iterator().next().getProgrammeId(), is(1L));
    assertThat("Unexpected associated trust count.", profileUser.getAssociatedTrusts().size(),
        is(1));
    assertThat("Unexpected associated trust id.",
        profileUser.getAssociatedTrusts().iterator().next().getTrustId(), is(2L));
  }

  @Test
  public void shouldNotPublishDeleteKeycloakUserEventWhenUserNotFound() {
    exceptionRule.expect(UserNotFoundException.class);
    exceptionRule.expectMessage(containsString("user1"));
    exceptionRule.expectMessage(containsString(KeyCloakAdminClientService.NAME));

    testClass.publishDeleteKeycloakUserRequestedEvent("user1");

    verify(applicationEventPublisher, never()).publishEvent(any());
  }

  @Test
  public void shouldPublishDeleteKeycloakUserEventWhenUserFound() {
    User kcUser = new User();
    when(keyCloakAdminClientService.getUser("user1")).thenReturn(Optional.of(kcUser));

    testClass.publishDeleteKeycloakUserRequestedEvent("user1");

    ArgumentCaptor<DeleteKeycloakUserRequestedEvent> eventCaptor = ArgumentCaptor.forClass(
        DeleteKeycloakUserRequestedEvent.class);
    verify(applicationEventPublisher).publishEvent(eventCaptor.capture());

    DeleteKeycloakUserRequestedEvent event = eventCaptor.getValue();
    assertThat("Unexpected user.", event.getKcUser(), is(kcUser));
    assertThat("Unexpected publish profile event flag.", event.isPublishDeleteProfileUserEvent(),
        is(true));
  }

  @Test
  public void shouldUpdatePassword() {
    UserPasswordDTO userPassword = new UserPasswordDTO();
    userPassword.setKcId("userId1");
    userPassword.setPassword("P4$$w0rd");
    userPassword.setTempPassword(true);

    testClass.updatePassword(userPassword);

    verify(keyCloakAdminClientService).updatePassword("userId1", "P4$$w0rd", true);
  }

  /**
   * Create a set of roles with the given names.
   *
   * @param roleNames The names of the new role.
   */
  private Set<RoleDTO> buildRoleDtos(String... roleNames) {
    Set<RoleDTO> roles = new HashSet<>();

    for (String roleName : roleNames) {
      RoleDTO roleDto = new RoleDTO();
      roleDto.setName(roleName);
      roles.add(roleDto);
    }

    return roles;
  }
}
