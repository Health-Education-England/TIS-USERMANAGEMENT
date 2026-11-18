package uk.nhs.hee.tis.usermanagement.resource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.transformuk.hee.tis.reference.api.dto.DBCDTO;
import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceView;
import uk.nhs.hee.tis.usermanagement.DTOs.UserDTO;
import uk.nhs.hee.tis.usermanagement.DTOs.UserPasswordDTO;
import uk.nhs.hee.tis.usermanagement.facade.UserManagementFacade;

@SpringBootTest
class UserManagementControllerTest {

  private MockMvc mockMvc;
  @MockBean
  UserManagementFacade mockFacade;
  @Captor
  private ArgumentCaptor<String> stringCaptor;
  @Captor
  private ArgumentCaptor<UserDTO> userDtoCaptor;
  @Captor
  private ArgumentCaptor<UserPasswordDTO> userPasswordDtoCaptor;

  @BeforeEach
  void setup() {
    mockMvc = MockMvcBuilders.standaloneSetup(new UserManagementController(mockFacade, "keycloak"))
        .setSingleView(new InternalResourceView("emptyView"))
        .build();
  }

  @Test
  void shouldReturnEditFormForValidUser() throws Exception {
    final UserDTO user = new UserDTO();
    when(mockFacade.getCompleteUser(stringCaptor.capture())).thenReturn(user);
    List<String> rolesList = new ArrayList<>();
    when(mockFacade.getAllAssignableRoles()).thenReturn(rolesList);
    List<DBCDTO> dbcList = new ArrayList<>();
    when(mockFacade.getAllDBCs()).thenReturn(dbcList);
    List<TrustDTO> trustList = new ArrayList<>();
    when(mockFacade.getAllTrusts()).thenReturn(trustList);
    List<ProgrammeDTO> programmeList = new ArrayList<>();
    when(mockFacade.getAllProgrammes()).thenReturn(programmeList);
    List<String> entityList = new ArrayList<>();
    when(mockFacade.getAllEntityRoles()).thenReturn(entityList);
    mockMvc.perform(get("/user").param("userName", "value"))
        .andExpectAll(
            status().is(200),
            view().name("userEdit"),
            model().attribute("user", user),
            model().attribute("roles", rolesList),
            model().attribute("entityRoles", entityList),
            model().attribute("designatedBodyCodes", dbcList),
            model().attribute("trusts", trustList),
            model().attribute("programmes", programmeList)
        );

    assertThat(stringCaptor.getValue(), is("value"));
  }

  @Test
  void shouldReturnRolesForUsers() throws Exception {
    UserDTO one = new UserDTO();
    when(mockFacade.getUserByNameIgnoreCase("one")).thenReturn(one);
    UserDTO two = new UserDTO();
    when(mockFacade.getUserByNameIgnoreCase("two")).thenReturn(two);
    UserDTO four = new UserDTO();
    when(mockFacade.getUserByNameIgnoreCase("four")).thenReturn(four);

    mockMvc.perform(
            get("/rolesForUsers").param("search", "one two three four"))
        .andExpect(model().attribute("users", containsInAnyOrder(one, two, four)));
  }


  @Test
  void shouldReturnBlankUserForCreateUser() throws Exception {
    List<String> rolesList = List.of("foo");
    when(mockFacade.getAllAssignableRoles()).thenReturn(rolesList);
    List<DBCDTO> dbcList = new ArrayList<>();
    when(mockFacade.getAllDBCs()).thenReturn(dbcList);
    List<TrustDTO> trustList = new ArrayList<>();
    when(mockFacade.getAllTrusts()).thenReturn(trustList);
    List<ProgrammeDTO> programmeList = new ArrayList<>();
    when(mockFacade.getAllProgrammes()).thenReturn(programmeList);
    List<String> entityList = new ArrayList<>();
    when(mockFacade.getAllEntityRoles()).thenReturn(entityList);
    mockMvc.perform(get("/createUser"))
        .andExpectAll(
            status().isOk(),
            view().name("createUser"),
            model().attributeExists("user"),
            model().attribute("roles", rolesList),
            model().attribute("entityRoles", entityList),
            model().attribute("designatedBodyCodes", dbcList),
            model().attribute("trusts", trustList),
            model().attribute("programmes", programmeList)
        );
  }

  @Test
  void shouldPublishEventForNewUser() throws Exception {
    when(mockFacade.getUserByNameIgnoreCase("foo")).thenReturn(null);
    mockMvc.perform(
            post("/createUser").param("name", "foo").param("emailAddress", "name@tis.nhs.uk")
                .param("password", "huntER2!").param("confirmPassword", "huntER2!"))
        .andExpect(status().is2xxSuccessful())
        .andExpect(model().attribute("message",
            containsString("A request for user null null (foo) has been made.")));
    verify(mockFacade).publishUserCreationRequestedEvent(any());
  }

  @ParameterizedTest
  @MethodSource("mismatchedPasswordInputs")
  void shouldRejectCreateUserWhenPasswordsDoNotMatch(String password, String confirmPassword) {
    Exception exception = assertThrows(Exception.class, () -> mockMvc.perform(
        post("/createUser")
            .param("name", "foo")
            .param("emailAddress", "name@tis.nhs.uk")
            .param("password", password)
            .param("confirmPassword", confirmPassword)));

    assertNestedCauseMessage(exception, "Passwords do not match");
    verify(mockFacade, never()).publishUserCreationRequestedEvent(any());
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "short", "1234567"})
  void shouldRejectCreateUserWhenPasswordTooShort(String password) {
    Exception exception = assertThrows(Exception.class, () -> mockMvc.perform(
        post("/createUser")
            .param("name", "foo")
            .param("emailAddress", "name@tis.nhs.uk")
            .param("password", password)
            .param("confirmPassword", password)));

    assertNestedCauseMessage(exception, "Password needs to be at least 8 chars long");
    verify(mockFacade, never()).publishUserCreationRequestedEvent(any());
  }

  @ParameterizedTest
  @MethodSource("whitespaceCreateUserInputs")
  void shouldRejectCreateUserWhenUsernameOrEmailContainsWhitespace(String username,
      String emailAddress, String messagePart) {
    Exception exception = assertThrows(Exception.class, () -> mockMvc.perform(
        post("/createUser")
            .param("name", username)
            .param("emailAddress", emailAddress)
            .param("password", "huntER2!")
            .param("confirmPassword", "huntER2!")));

    assertNestedCauseMessage(exception, messagePart);
    verify(mockFacade, never()).publishUserCreationRequestedEvent(any());
  }

  @Test
  void shouldRejectCreateUserWhenUsernameAlreadyExists() {
    when(mockFacade.getUserByNameIgnoreCase("foo")).thenReturn(new UserDTO());

    Exception exception = assertThrows(Exception.class, () -> mockMvc.perform(
        post("/createUser")
            .param("name", "foo")
            .param("emailAddress", "name@tis.nhs.uk")
            .param("password", "huntER2!")
            .param("confirmPassword", "huntER2!")));

    assertNestedCauseMessage(exception, "the username already exists");
    verify(mockFacade, never()).publishUserCreationRequestedEvent(any());
  }

  @Test
  void shouldPublishEventForUpdateUser() throws Exception {
    when(mockFacade.getUserByNameIgnoreCase("foo")).thenReturn(null);
    mockMvc.perform(
            post("/updateUser").param("name", "foo"))
        .andExpect(status().is2xxSuccessful())
        .andExpect(model().attribute("message", "The user null null (foo) has been updated"));
    verify(mockFacade).updateSingleUser(userDtoCaptor.capture());
  }

  @Test
  void shouldPublishEventForUpdatePassword() throws Exception {
    when(mockFacade.getUserByNameIgnoreCase("foo")).thenReturn(null);
    mockMvc.perform(
            post("/updatePassword")
                .param("kcId", "foo")
                .param("password", "huntER2!")
                .param("confirmPassword", "huntER2!"))
        .andExpect(status().is2xxSuccessful())
        .andExpect(model().attribute("message", "Password has been updated for the user"));
    verify(mockFacade).updatePassword(userPasswordDtoCaptor.capture());
    UserPasswordDTO actual = userPasswordDtoCaptor.getValue();
    assertThat(actual.getPassword(), is("huntER2!"));
    assertThat(actual.getKcId(), is("foo"));
  }

  @ParameterizedTest
  @MethodSource("mismatchedPasswordInputs")
  void shouldRejectUpdatePasswordWhenPasswordsDoNotMatch(String password, String confirmPassword) {
    Exception exception = assertThrows(Exception.class, () -> mockMvc.perform(
        post("/updatePassword")
            .param("kcId", "foo")
            .param("password", password)
            .param("confirmPassword", confirmPassword)));

    assertNestedCauseMessage(exception, "Passwords do not match");
    verify(mockFacade, never()).updatePassword(any());
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "short", "1234567"})
  void shouldRejectUpdatePasswordWhenPasswordTooShort(String password) {
    Exception exception = assertThrows(Exception.class, () -> mockMvc.perform(
        post("/updatePassword")
            .param("kcId", "foo")
            .param("password", password)
            .param("confirmPassword", password)));

    assertNestedCauseMessage(exception, "Password needs to be at least 8 chars long");
    verify(mockFacade, never()).updatePassword(any());
  }

  @Test
  void shouldPublishEventForDelete() throws Exception {
    when(mockFacade.getUserByNameIgnoreCase("foo")).thenReturn(null);
    mockMvc.perform(
            post("/deleteUser").param("name", "foo"))
        .andExpect(status().is2xxSuccessful())
        .andExpect(model().attribute("message",
            containsString("The user foo has been deleted.")));
    verify(mockFacade).publishDeleteAuthenticationUserRequestedEvent("foo");
  }

  @Test
  void shouldReturnUserNotificationView() throws Exception {
    mockMvc.perform(
            get("/userNotification"))
        .andExpectAll(
            status().isOk(),
            view().name("userNotification"));
  }

  private void assertNestedCauseMessage(Exception exception, String expectedMessagePart) {
    Throwable cause = exception.getCause();
    assertNotNull(cause);
    assertThat(cause.getMessage(), containsString(expectedMessagePart));
  }

  private static Stream<Arguments> mismatchedPasswordInputs() {
    return Stream.of(
        Arguments.of("huntER2!", "wrongPwd2!"),
        Arguments.of("password1", "password2")
    );
  }

  private static Stream<Arguments> whitespaceCreateUserInputs() {
    return Stream.of(
        Arguments.of("bad user", "name@tis.nhs.uk", "username shouldn't contain white spaces"),
        Arguments.of("foo", "name @tis.nhs.uk", "email address shouldn't contain white spaces")
    );
  }
}
