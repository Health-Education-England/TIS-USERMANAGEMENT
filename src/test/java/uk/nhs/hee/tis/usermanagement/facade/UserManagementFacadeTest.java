package uk.nhs.hee.tis.usermanagement.facade;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import uk.nhs.hee.tis.usermanagement.DTOs.AuthenticationUserDto;
import uk.nhs.hee.tis.usermanagement.DTOs.CreateUserDTO;
import uk.nhs.hee.tis.usermanagement.DTOs.UserDTO;
import uk.nhs.hee.tis.usermanagement.DTOs.UserPasswordDTO;
import uk.nhs.hee.tis.usermanagement.event.CreateAuthenticationUserRequestedEvent;
import uk.nhs.hee.tis.usermanagement.event.DeleteAuthenticationUserRequestedEvent;
import uk.nhs.hee.tis.usermanagement.exception.IdentityProviderException;
import uk.nhs.hee.tis.usermanagement.exception.UpdateUserException;
import uk.nhs.hee.tis.usermanagement.exception.UserNotFoundException;
import uk.nhs.hee.tis.usermanagement.mapper.HeeUserMapper;
import uk.nhs.hee.tis.usermanagement.service.AuthenticationAdminService;
import uk.nhs.hee.tis.usermanagement.service.ProfileService;
import uk.nhs.hee.tis.usermanagement.service.ReferenceService;
import uk.nhs.hee.tis.usermanagement.service.TcsService;
import uk.nhs.hee.tis.usermanagement.util.PasswordUtil;

@ExtendWith(MockitoExtension.class)
class UserManagementFacadeTest {

  private static final String USERNAME = "user1";

  @InjectMocks
  UserManagementFacade testClass;

  @Mock
  ProfileService profileService;

  @Mock
  ReferenceService referenceService;

  @Mock
  TcsService tcsService;

  @Mock
  AuthenticationAdminService authenticationAdminService;

  @Mock
  ApplicationEventPublisher applicationEventPublisher;

  @Mock
  PasswordUtil passwordUtil;

  @Spy
  private HeeUserMapper userMapper;

  @BeforeEach
  public void setup() {
    ReflectionTestUtils.setField(passwordUtil, "PASSWORD_UPPERCASE_MINIMUM", 1);
    ReflectionTestUtils.setField(passwordUtil, "PASSWORD_LOWERCASE_MINIMUM", 1);
    ReflectionTestUtils.setField(passwordUtil, "PASSWORD_DIGIT_MINIMUM", 1);
    ReflectionTestUtils.setField(passwordUtil, "PASSWORD_SPECIAL_MINIMUM", 1);
    ReflectionTestUtils.setField(passwordUtil, "PASSWORD_LENGTH_MINIMUM", 12);
  }

  @Test
  void shouldGetAllAssignableRoles() {
    String adminRole = "HEE TIS Admin";
    String etlRole = "ETL";
    String rvAdmin = "RVAdmin";
    List<String> mockRoles = Lists.newArrayList(adminRole, rvAdmin, etlRole);

    when(profileService.getAllAssignableRoles()).thenReturn(mockRoles);
    List<String> actual = testClass.getAllAssignableRoles();
    assertThat(actual, containsInAnyOrder(adminRole, rvAdmin, etlRole));
    verify(profileService).getAllAssignableRoles();
  }

  @Test
  void shouldGetAllEntityRoles() {
    List<String> entityRoles = testClass.getAllEntityRoles();
    String hee = "HEE";
    assertThat(entityRoles, containsInAnyOrder(hee));
  }

  @Test
  void testGetNullUserByNameWhenUserNotFound() {
    // Record expectations.
    when(profileService.getUserByUsernameIgnoreCase("testUser_1")).thenReturn(Optional.empty());

    // Call the code under test.
    UserDTO user = testClass.getUserByNameIgnoreCase("testUser_1");

    // Perform assertions.
    assertThat("The user did not match the expected value.", user, nullValue());
  }

  @Test
  void shouldGetUserByNameIgnoringCaseWhenUserFound() {
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
  void shouldThrowExceptionGettingCompleteUserWhenUserNotFoundInProfile() {
    when(authenticationAdminService.getUser(USERNAME)).thenReturn(
        Optional.of(new AuthenticationUserDto()));

    UserNotFoundException actual = assertThrows(UserNotFoundException.class,
        () -> testClass.getCompleteUser(USERNAME));
    assertThat(actual.getMessage(), containsString(USERNAME));
    assertThat(actual.getMessage(), containsString(ProfileService.NAME));
  }

  @Test
  void shouldThrowExceptionGettingCompleteUserWhenUserNotFoundInAuthService() {
    when(profileService.getUserByUsername(USERNAME)).thenReturn(Optional.of(new HeeUserDTO()));
    when(authenticationAdminService.getServiceName()).thenReturn("TEST");

    UserNotFoundException actual = assertThrows(UserNotFoundException.class,
        () -> testClass.getCompleteUser(USERNAME));
    assertThat(actual.getMessage(), containsString(USERNAME));
    assertThat(actual.getMessage(), containsString(authenticationAdminService.getServiceName()));
  }

  @Test
  void shouldGetCompleteUserWhenUserFound() {
    HeeUserDTO heeUser = new HeeUserDTO();
    heeUser.setName(USERNAME);

    final String assignableRole = "assignableRole";
    final String restrictedRole = "restrictedRole";
    RoleDTO role1 = new RoleDTO();
    role1.setName(assignableRole);
    RoleDTO role2 = new RoleDTO();
    role2.setName(restrictedRole);
    heeUser.setRoles(Sets.newHashSet(role1, role2));

    AuthenticationUserDto authenticationUser = new AuthenticationUserDto();
    authenticationUser.setId("userId1");
    authenticationUser.setEnabled(true);

    when(profileService.getUserByUsername(USERNAME)).thenReturn(Optional.of(heeUser));
    when(authenticationAdminService.getUser(USERNAME)).thenReturn(Optional.of(authenticationUser));
    when(profileService.getRestrictedRoles()).thenReturn(Set.of(restrictedRole));

    UserDTO user = testClass.getCompleteUser(USERNAME);
    assertThat("Unexpected user id.", user.getAuthId(), is("userId1"));
    assertThat("Unexpected user name.", user.getName(), is(USERNAME));
    assertThat("Unexpected user enabled flag.", user.getActive(), is(true));
    assertThat("Unexpected size of roles.", user.getRoles().size(), is(1));
    assertThat("", user.getRoles().iterator().next(), is(assignableRole));
  }

  @ParameterizedTest
  @CsvSource({"false,true", "true,false"})
  void shouldPreferActiveFromAuthenticationService(boolean profileStatus, boolean authNStatus) {
    HeeUserDTO heeUser = new HeeUserDTO();
    heeUser.setName(USERNAME);
    heeUser.setActive(profileStatus);

    AuthenticationUserDto authenticationUser = new AuthenticationUserDto();
    authenticationUser.setId("userId1");
    authenticationUser.setEnabled(authNStatus);

    when(profileService.getUserByUsername(USERNAME)).thenReturn(Optional.of(heeUser));
    when(authenticationAdminService.getUser(USERNAME)).thenReturn(Optional.of(authenticationUser));
    when(profileService.getRestrictedRoles()).thenReturn(Set.of());

    UserDTO user = testClass.getCompleteUser(USERNAME);
    assertThat("Unexpected user id.", user.getAuthId(), is("userId1"));
    assertThat("Unexpected user name.", user.getName(), is(USERNAME));
    assertThat("Unexpected user enabled flag.", user.getActive(), is(authNStatus));
  }

  @Test
  void shouldGetAllUsers() {
    HeeUserDTO profileUser1 = new HeeUserDTO();
    profileUser1.setName(USERNAME);
    profileUser1.setActive(true);
    AuthenticationUserDto authenticationUser1 = new AuthenticationUserDto();
    authenticationUser1.setId("1");
    authenticationUser1.setUsername(USERNAME);
    authenticationUser1.setEnabled(true);
    HeeUserDTO profileUser2 = new HeeUserDTO();
    profileUser2.setName("user2");
    Page<HeeUserDTO> profileUsers = new PageImpl<>(Arrays.asList(profileUser1, profileUser2));

    when(profileService.getAllUsers(Pageable.unpaged(), "searchString")).thenReturn(profileUsers);
    when(authenticationAdminService.getUser(USERNAME)).thenReturn(Optional.of(authenticationUser1));
    when(authenticationAdminService.getUser("user2")).thenReturn(Optional.empty());

    Page<UserDTO> allUsersPage = testClass.getAllUsers(Pageable.unpaged(), "searchString");

    assertThat("Unexpected user count.", allUsersPage.getTotalElements(), is(2L));
    assertThat("Unexpected pageable.", allUsersPage.getPageable(), is(Pageable.unpaged()));

    assertThat(allUsersPage.stream().filter(u -> u.getAuthId() != null).map(UserDTO::getName)
        .collect(Collectors.toSet()), containsInAnyOrder(USERNAME));
    Set<String> allUserNames = allUsersPage.stream()
        .map(UserDTO::getName)
        .collect(Collectors.toSet());
    assertThat("Unexpected user names.", allUserNames, hasItems(USERNAME, "user2"));
    List<Boolean> userActiveFlags = allUsersPage.stream()
        .map(UserDTO::getActive)
        .collect(Collectors.toList());
    assertThat("Unexpected Active Flags", userActiveFlags, containsInAnyOrder(true, false));

  }

  @Test
  void shouldThrowExceptionUpdatingSingleUserWhenUsernameNotFoundInAuthService() {
    UserDTO user = new UserDTO();
    user.setName(USERNAME);

    when(authenticationAdminService.getServiceName()).thenReturn("TEST");

    UserNotFoundException actual = assertThrows(UserNotFoundException.class,
        () -> testClass.updateSingleUser(user));
    assertThat(actual.getMessage(), containsString(USERNAME));
    assertThat(actual.getMessage(), containsString(authenticationAdminService.getServiceName()));
  }

  @Test
  void shouldThrowExceptionUpdatingSingleUserWhenAuthServiceUpdateFails() {
    UserDTO user = new UserDTO();
    user.setName(USERNAME);

    when(authenticationAdminService.getUser(USERNAME)).thenReturn(
        Optional.of(new AuthenticationUserDto()));
    when(authenticationAdminService.getServiceName()).thenReturn("TEST");

    UpdateUserException actual = assertThrows(UpdateUserException.class,
        () -> testClass.updateSingleUser(user));
    assertThat(actual.getMessage(), containsString(USERNAME));
    assertThat(actual.getMessage(), containsString(authenticationAdminService.getServiceName()));
  }

  @Test
  void shouldUpdateSingleAuthServiceUserAndThrowExceptionWhenUsernameNotFoundInProfile() {
    UserDTO user = new UserDTO();
    user.setName(USERNAME);

    when(authenticationAdminService.getUser(USERNAME)).thenReturn(
        Optional.of(new AuthenticationUserDto()));
    when(authenticationAdminService.updateUser(user)).thenReturn(true);

    UserNotFoundException actual = assertThrows(UserNotFoundException.class,
        () -> testClass.updateSingleUser(user));
    assertThat(actual.getMessage(), containsString(USERNAME));
    assertThat(actual.getMessage(), containsString(ProfileService.NAME));
    verify(authenticationAdminService).updateUser(user);
  }

  @Test
  void shouldNotUpdateSingleAuthServiceUserAndThrowExceptionWhenProfileUpdateFails() {
    UserDTO user = new UserDTO();
    user.setName(USERNAME);

    AuthenticationUserDto authenticationUser = new AuthenticationUserDto();
    authenticationUser.setId("userId1");

    HeeUserDTO heeUser = new HeeUserDTO();
    heeUser.setRoles(Collections.emptySet());

    when(authenticationAdminService.getUser(USERNAME)).thenReturn(Optional.of(authenticationUser));
    when(authenticationAdminService.updateUser(user)).thenReturn(true);

    when(profileService.getUserByUsername(USERNAME)).thenReturn(Optional.of(heeUser));

    UpdateUserException actual = assertThrows(UpdateUserException.class,
        () -> testClass.updateSingleUser(user));
    assertThat(actual.getMessage(), containsString(USERNAME));
    assertThat(actual.getMessage(), containsString(ProfileService.NAME));
    // Verify the rollback.
    verify(authenticationAdminService).updateUser(authenticationUser);
  }

  @Test
  void shouldUpdateSingleUserWhenAuthServiceAndProfileUpdatesSucceed() {
    UserDTO user = new UserDTO();
    user.setName(USERNAME);
    Set<String> newRoles = new HashSet<>(Arrays.asList("role1", "role2"));
    user.setRoles(newRoles);

    AuthenticationUserDto authenticationUser = new AuthenticationUserDto();
    authenticationUser.setId("userId1");

    HeeUserDTO existingHeeUser = new HeeUserDTO();
    existingHeeUser.setRoles(buildRoleDtos("role1", "role3", "Machine User"));

    when(authenticationAdminService.getUser(USERNAME)).thenReturn(Optional.of(authenticationUser));
    when(authenticationAdminService.updateUser(user)).thenReturn(true);

    when(profileService.getUserByUsername(USERNAME)).thenReturn(Optional.of(existingHeeUser));
    when(profileService.getRestrictedRoles()).thenReturn(
        Set.of("Machine User", "RVOfficer", "HEE"));
    ArgumentCaptor<HeeUserDTO> updatedHeeUserCaptor = ArgumentCaptor.forClass(HeeUserDTO.class);
    when(profileService.updateUser(updatedHeeUserCaptor.capture())).thenReturn(
        Optional.of(new HeeUserDTO()));

    testClass.updateSingleUser(user);

    HeeUserDTO updatedHeeUser = updatedHeeUserCaptor.getValue();
    assertThat("Unexpected user name.", updatedHeeUser.getName(), is(USERNAME));

    Set<String> updatedRoles = updatedHeeUser.getRoles().stream().map(RoleDTO::getName)
        .collect(Collectors.toSet());
    assertThat("Unexpected role count.", updatedRoles.size(), is(3));
    assertThat("Unexpected roles.", updatedRoles, hasItems("role1", "role2", "Machine User"));

    // Verify no rollback.
    verify(authenticationAdminService, never()).updateUser(authenticationUser);
  }

  @Test
  void shouldPublishUserCreationEvent() {
    CreateUserDTO createUser = new CreateUserDTO();
    createUser.setName(USERNAME);
    createUser.setAssociatedProgrammes(new HashSet<>(Collections.singleton("1")));
    createUser.setAssociatedTrusts(new HashSet<>(Collections.singleton("2")));

    ProgrammeDTO programme = new ProgrammeDTO();
    programme.setId(1L);
    when(tcsService.getAllProgrammes()).thenReturn(Collections.singletonList(programme));

    TrustDTO trust = new TrustDTO();
    trust.setId(2L);
    when(referenceService.getAllTrusts()).thenReturn(Collections.singletonList(trust));

    testClass.publishUserCreationRequestedEvent(createUser);

    ArgumentCaptor<CreateAuthenticationUserRequestedEvent> eventCaptor = ArgumentCaptor.forClass(
        CreateAuthenticationUserRequestedEvent.class);
    verify(applicationEventPublisher).publishEvent(eventCaptor.capture());

    CreateAuthenticationUserRequestedEvent event = eventCaptor.getValue();
    assertThat("Unexpected user.", event.getUserDTO(), is(createUser));

    HeeUserDTO profileUser = event.getUserToCreateInProfileService();
    assertThat("Unexpected user name.", profileUser.getName(), is(USERNAME));
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
  void shouldNotPublishDeleteAuthServiceUserEventWhenUserNotFound() {
    when(authenticationAdminService.getServiceName()).thenReturn("TEST");

    UserNotFoundException actual = assertThrows(UserNotFoundException.class,
        () -> testClass.publishDeleteAuthenticationUserRequestedEvent(USERNAME));
    assertThat(actual.getMessage(), containsString(USERNAME));
    assertThat(actual.getMessage(), containsString(authenticationAdminService.getServiceName()));

    verify(applicationEventPublisher, never()).publishEvent(any());
  }

  @Test
  void shouldPublishDeleteAuthServiceUserEventWhenUserFound() {
    AuthenticationUserDto authenticationUser = new AuthenticationUserDto();
    when(authenticationAdminService.getUser(USERNAME)).thenReturn(Optional.of(authenticationUser));

    testClass.publishDeleteAuthenticationUserRequestedEvent(USERNAME);

    ArgumentCaptor<DeleteAuthenticationUserRequestedEvent> eventCaptor = ArgumentCaptor.forClass(
        DeleteAuthenticationUserRequestedEvent.class);
    verify(applicationEventPublisher).publishEvent(eventCaptor.capture());

    DeleteAuthenticationUserRequestedEvent event = eventCaptor.getValue();
    assertThat("Unexpected user.", event.getAuthenticationUser(), is(authenticationUser));
    assertThat("Unexpected publish profile event flag.", event.isPublishDeleteProfileUserEvent(),
        is(true));
  }

  @Test
  void shouldUpdatePassword() {
    UserPasswordDTO userPassword = new UserPasswordDTO();
    userPassword.setKcId("userId1");
    userPassword.setPassword("P4$$w0rd");
    userPassword.setTempPassword(true);

    testClass.updatePassword(userPassword);

    verify(authenticationAdminService).updatePassword("userId1", "P4$$w0rd", true);
  }

  @Test
  void shouldGetAuthEventsForUser() {
    testClass.getUserAuthEvents(USERNAME);

    verify(authenticationAdminService).getUserAuthEvents(USERNAME);
  }

  @Test
  void shouldThrowIdentityProviderExceptionOnGetAuthEventsForUser() {
    when(authenticationAdminService.getUserAuthEvents(USERNAME)).thenThrow(
        RuntimeException.class);

    assertThrows(IdentityProviderException.class, () -> testClass.getUserAuthEvents(USERNAME));
  }

  @Test
  void shouldTriggerPasswordReset() {
    when(passwordUtil.generatePassword()).thenReturn("Password1!");

    String password = testClass.triggerPasswordReset(USERNAME);

    verify(authenticationAdminService).updatePassword(eq(USERNAME), any(String.class), eq(true));
    Assertions.assertNotNull(password);
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
