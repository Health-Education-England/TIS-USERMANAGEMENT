package uk.nhs.hee.tis.usermanagement.resource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InvalidParameterException;
import software.amazon.awssdk.services.ses.model.MessageRejectedException;
import uk.nhs.hee.tis.usermanagement.DTOs.CreateUserDTO;
import uk.nhs.hee.tis.usermanagement.DTOs.UserAuthEventDto;
import uk.nhs.hee.tis.usermanagement.DTOs.UserDTO;
import uk.nhs.hee.tis.usermanagement.exception.IdentityProviderException;
import uk.nhs.hee.tis.usermanagement.exception.UpdateUserException;
import uk.nhs.hee.tis.usermanagement.exception.UserNotFoundException;
import uk.nhs.hee.tis.usermanagement.facade.UserManagementFacade;
import uk.nhs.hee.tis.usermanagement.service.EmailService;

@SpringBootTest
class UserResourceTest {

  private static final String TRUST_OBSERVER = "HEE Trust Observer";
  private static final String TRUST_ADMIN = "HEE Trust Admin";
  private static final String PROGRAMME_OBSERVER = "HEE Programme Observer";
  private static final String PROGRAMME_ADMIN = "HEE Programme Admin";
  private static final String TRUST_A = "Trust A";
  private static final String PROGRAMME_A = "Programme A";

  private static final String TRUST_REQUIRED_MESSAGE =
      "At least one Trust must be selected for the role";
  private static final String PROGRAMME_REQUIRED_MESSAGE =
      "At least one programme must be selected for the role";

  private MockMvc mockMvc;

  @MockBean
  private UserManagementFacade mockFacade;
  @MockBean
  private EmailService emailServiceMock;
  @Mock
  private Page<UserDTO> mockUsersPage;

  @Autowired
  private UserResourceAdvice advice;
  @Autowired
  private MappingJackson2HttpMessageConverter jacksonMessageConverter;
  @Autowired
  private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

  @Captor
  private ArgumentCaptor<UserDTO> userCaptor;
  @Captor
  private ArgumentCaptor<CreateUserDTO> createCaptor;
  @Captor
  private ArgumentCaptor<Pageable> pageableCaptor;
  @Captor
  private ArgumentCaptor<String> stringCaptor;
  private UserDTO defaultUser;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(new UserResource(mockFacade, emailServiceMock))
        .setCustomArgumentResolvers(pageableArgumentResolver)
        .setControllerAdvice(advice)
        .setMessageConverters(jacksonMessageConverter).build();

    defaultUser = new UserDTO();
    defaultUser.setName("testuser");
    defaultUser.setEmailAddress("urtu@tis.nhs.uk");
  }

  @Test
  void shouldFindPagedUsers() throws Exception {
    when(mockFacade.getAllUsers(pageableCaptor.capture(), isNull())).thenReturn(mockUsersPage);
    when(mockUsersPage.getContent()).thenReturn(List.of(defaultUser, defaultUser, defaultUser));

    mockMvc.perform(
            get("/api/users").param("page", "2").param("size", "5").param("sort", "emailAddress,desc"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(3));

    Pageable actual = pageableCaptor.getValue();
    assertThat(actual.getPageNumber(), is(2));
    assertThat(actual.getPageSize(), is(5));
    assertThat(actual.getSort().stream().count(), is(1L));
    assertThat(actual.getSort().stream().findFirst().isPresent(), is(true));
    Order actualOrder = actual.getSort().stream().findFirst().get();
    assertThat(actualOrder.getProperty(), is("emailAddress"));
    assertThat(actualOrder.getDirection(), is(Direction.DESC));
  }

  @Test
  void shouldFindUsersByName() throws Exception {
    when(mockFacade.getAllUsers(pageableCaptor.capture(), stringCaptor.capture())).thenReturn(
        mockUsersPage);
    when(mockUsersPage.getContent()).thenReturn(List.of(defaultUser, defaultUser, defaultUser));

    mockMvc.perform(get("/api/users").param("search", "term"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(3));
    assertThat(stringCaptor.getValue(), is("term"));
  }

  @Test
  void shouldGetUserByName() throws Exception {
    when(mockFacade.getCompleteUser("foo")).thenReturn(defaultUser);

    mockMvc.perform(get("/api/users/foo"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("testuser"))
        .andExpect(jsonPath("$.emailAddress").value("urtu@tis.nhs.uk"));
  }

  @Test
  void shouldRespondBadRequestWhenGettingNonexistentUser() throws Exception {
    when(mockFacade.getCompleteUser("foo")).thenThrow(new UserNotFoundException("foo", "testSvc"));

    mockMvc.perform(get("/api/users/foo"))
        .andExpect(status().isInternalServerError())
        .andExpect(
            content().string(stringContainsInOrder("Could not find user", "foo", "testSvc")));
  }

  @Test
  void shouldCreateUser() throws Exception {
    String newUserJson = "{ \"name\": \"newUser\", \"password\": \"hunter1\" }";
    mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(newUserJson))
        .andExpect(status().isAccepted());

    verify(mockFacade).publishUserCreationRequestedEvent(createCaptor.capture());
    CreateUserDTO actual = createCaptor.getValue();
    assertThat(actual.getName(), is("newUser"));
    assertThat(actual.getPassword(), is("hunter1"));
  }

  @Test
  void shouldRespondBadRequestWhenCreatingExistingUser() throws Exception {
    when(mockFacade.getUserByNameIgnoreCase("newuser")).thenReturn(defaultUser);

    String newUserJson = "{ \"name\": \"newuser\", \"emailAddress\": \"thisis@valid.email\" }";
    mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(newUserJson))
        .andExpect(status().isInternalServerError())
        .andExpect(
            content().string(is("\"Cannot create user because the username already exists.\"")));

    verify(mockFacade, never()).publishUserCreationRequestedEvent(any(CreateUserDTO.class));
  }

  @Test
  void shouldRespondBadRequestWhenWhitespaceInName() throws Exception {
    String newUserJson = "{ \"name\": \"new user\", \"password\": \"hunter1\" }";
    mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(newUserJson))
        .andExpect(status().isInternalServerError())
        .andExpect(
            content().string(stringContainsInOrder("name", "whitespace")));

    verifyNoInteractions(mockFacade);
  }

  @Test
  void shouldRespondBadRequestWhenWhitespaceInEmail() throws Exception {
    String newUserJson = "{ \"name\": \"newuser\", \"emailAddress\": \"this is@valid.email\" }";
    mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(newUserJson))
        .andExpect(status().isInternalServerError())
        .andExpect(
            content().string(stringContainsInOrder("Email", "whitespace")));

    verifyNoInteractions(mockFacade);
  }

  @Test
  void shouldUpdateUser() throws Exception {
    String updatedUserJson = "{ \"name\": \"foo\", \"firstName\": \"bar\" }";
    mockMvc.perform(put("/api/users/foo")
            .contentType(MediaType.APPLICATION_JSON)
            .content(updatedUserJson))
        .andExpect(status().isAccepted());

    verify(mockFacade).updateSingleUser(userCaptor.capture());
    UserDTO actual = userCaptor.getValue();
    assertThat(actual.getName(), is("foo"));
    assertThat(actual.getFirstName(), is("bar"));
  }

  @Test
  void shouldRespondBadRequestWhenUpdatingWithDifferentUsername() throws Exception {
    String updatedUserJson = "{ \"name\": \"bar\", \"firstName\": \"updated\" }";

    mockMvc.perform(put("/api/users/foo")
            .contentType(MediaType.APPLICATION_JSON)
            .content(updatedUserJson))
        .andExpect(status().isInternalServerError())
        .andExpect(content().string("\"Username in request body must match username in path.\""));

    verify(mockFacade, never()).updateSingleUser(any(UserDTO.class));
  }

  @Test
  void shouldRespondBadRequestWhenUpdatingWithDifferentEmail() throws Exception {
    doThrow(new UpdateUserException("Email address in request body must match existing email."))
        .when(mockFacade).updateSingleUser(any(UserDTO.class));

    String updatedUserJson = "{ \"name\": \"foo\", \"emailAddress\": \"other@nhs.net\" }";
    mockMvc.perform(put("/api/users/foo")
            .contentType(MediaType.APPLICATION_JSON)
            .content(updatedUserJson))
        .andExpect(status().isInternalServerError())
        .andExpect(content().string("\"Email address in request body must match existing email.\""));
  }

  @Test
  void shouldRespondBadRequestWhenUpdatingNonexistentUser() throws Exception {
    doThrow(new UserNotFoundException("bah", "testSvc"))
        .when(mockFacade).updateSingleUser(userCaptor.capture());

    String updatedUserJson = "{ \"name\": \"foo\", \"firstName\": \"bar\" }";
    mockMvc.perform(put("/api/users/foo")
            .contentType(MediaType.APPLICATION_JSON)
            .content(updatedUserJson))
        .andExpect(status().isInternalServerError())
        .andExpect(
            content().string(stringContainsInOrder("Could not find user", "bah", "testSvc")));

    verify(mockFacade).updateSingleUser(userCaptor.capture());
    UserDTO actual = userCaptor.getValue();
    assertThat(actual.getName(), is("foo"));
    assertThat(actual.getFirstName(), is("bar"));
  }

  @Test
  void shouldDeleteUser() throws Exception {
    mockMvc.perform(delete("/api/users/foo"))
        .andExpect(status().isAccepted());

    verify(mockFacade).publishDeleteAuthenticationUserRequestedEvent("foo");
  }

  @Test
  void shouldRespondBadRequestWhenDeletingNonexistentUser() throws Exception {
    doThrow(new UserNotFoundException("bah", "testSvc"))
        .when(mockFacade).publishDeleteAuthenticationUserRequestedEvent("foo");

    mockMvc.perform(delete("/api/users/foo"))
        .andExpect(status().isInternalServerError())
        .andExpect(
            content().string(stringContainsInOrder("Could not find user", "bah", "testSvc")));
  }

  @Test
  void shouldGetAuthEventsForUser() throws Exception {
    Instant startTime = Instant.now();

    List<UserAuthEventDto> events = IntStream.range(0, 20)
        .mapToObj(n -> UserAuthEventDto.builder()
            .eventId(String.valueOf(n))
            .event("SignIn")
            .eventDate(
                Date.from(startTime.plusSeconds(n)))
            .result("Pass")
            .challenges("Password:Success, Mfa:Success")
            .device("Chrome 126, Windows 10")
            .build())
        .collect(Collectors.toList());

    when(mockFacade.getUserAuthEvents("foo")).thenReturn(events);

    MvcResult result = mockMvc.perform(
            get("/api/users/foo/authevents"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(20))
        .andReturn();

    String resultJson = result.getResponse().getContentAsString();
    Type type = new TypeToken<ArrayList<UserAuthEventDto>>(){}.getType();
    List<UserAuthEventDto> returnedData = new Gson().fromJson(resultJson, type);

    for (int i = 0; i < returnedData.size(); i++) {
      UserAuthEventDto data = returnedData.get(i);
      assertThat(data.getEventId(), CoreMatchers.is(String.valueOf(i)));
      assertThat(data.getEventDate(),
          CoreMatchers.is(Date.from(startTime.plusSeconds(i))));
      assertThat(data.getEvent(), CoreMatchers.is("SignIn"));
      assertThat(data.getResult(), CoreMatchers.is("Pass"));
      assertThat(data.getChallenges(), CoreMatchers.is("Password:Success, Mfa:Success"));
      assertThat(data.getDevice(), CoreMatchers.is("Chrome 126, Windows 10"));
    }
  }

  @Test
  void shouldRespondErrorWhenIdentityProviderThrowsException() throws Exception {
    doThrow(IdentityProviderException.class).when(mockFacade).getUserAuthEvents("foo");

    mockMvc.perform(
            get("/api/users/foo/authevents"))
        .andExpect(status().isInternalServerError());
  }

  @Test
  void shouldTriggerPasswordReset() throws Exception {
    when(mockFacade.triggerPasswordReset("foo")).thenReturn("password");

    mockMvc.perform(post("/api/users/foo/trigger-password-reset"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Password reset successfully"));
  }

  @Test
  void shouldTriggerPasswordResetReturnCognitoError() throws Exception {
    final String errorCode = "Invalid parameter";
    final String message = "error message";
    AwsErrorDetails errorDetails = AwsErrorDetails.builder().errorCode(errorCode).errorMessage(message).build();
    InvalidParameterException exception = InvalidParameterException.builder().statusCode(500).awsErrorDetails(errorDetails).build();

    doThrow(exception).when(mockFacade).triggerPasswordReset("foo");

    mockMvc.perform(post("/api/users/foo/trigger-password-reset"))
        .andExpect(status().is5xxServerError())
        .andExpect(jsonPath("$.errorCode").value(errorCode))
        .andExpect(jsonPath("$.errorType").value("Cognito Error"))
        .andExpect(jsonPath("$.message").value(message));
  }

  @Test
  void shouldTriggerPasswordResetReturnSesError() throws Exception {
    final String errorCode = "Message rejected";
    final String message = "error message";
    AwsErrorDetails errorDetails = AwsErrorDetails.builder().errorCode(errorCode).errorMessage(message).build();
    MessageRejectedException exception = MessageRejectedException.builder().statusCode(500).awsErrorDetails(errorDetails).build();

    doThrow(exception).when(mockFacade).triggerPasswordReset("foo");

    mockMvc.perform(post("/api/users/foo/trigger-password-reset"))
        .andExpect(status().is5xxServerError())
        .andExpect(jsonPath("$.errorCode").value(errorCode))
        .andExpect(jsonPath("$.errorType").value("SES Error"))
        .andExpect(jsonPath("$.message").value(message));
  }

  @Test
  void shouldTriggerPasswordResetReturnOtherError() throws Exception {
    final String message = "error message";
    RuntimeException exception = new RuntimeException(message);
    doThrow(exception).when(mockFacade).triggerPasswordReset("foo");

    mockMvc.perform(post("/api/users/foo/trigger-password-reset"))
        .andExpect(status().is5xxServerError())
        .andExpect(jsonPath("$.errorCode").value("Internal Server Error"))
        .andExpect(jsonPath("$.errorType").value("Internal Server Error"))
        .andExpect(jsonPath("$.message").value(message));
  }

  @Test
  void shouldResetUserMfaSettings() throws Exception {
    doNothing().when(mockFacade).resetUserMfaSettings("foo");

    mockMvc.perform(post("/api/users/foo/mfa/reset"))
        .andExpect(status().isOk());
  }

  @Test
  void shouldReturnErrorWhenResetUserMfaSettingsFails() throws Exception {
    UserNotFoundException exception = new UserNotFoundException("foo", "Cognito");
    doThrow(exception).when(mockFacade).resetUserMfaSettings("foo");

    mockMvc.perform(post("/api/users/foo/mfa/reset"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$").value(exception.getMessage()));
  }

  @ParameterizedTest
  @MethodSource("invalidRoleAssociations")
  void shouldRejectCreateWhenRoleHasNoRequiredAssociation(
      String role, String expectedMessage) throws Exception {

    CreateUserDTO user = new CreateUserDTO();
    user.setName("newuser");
    user.setEmailAddress("newuser@nhs.net");
    user.setRoles(Set.of(role));

    String requestBody = jacksonMessageConverter.getObjectMapper()
        .writeValueAsString(user);

    mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isInternalServerError())
        .andExpect(content().string(
            "\"" + expectedMessage + "\""));

    verify(mockFacade, never())
        .publishUserCreationRequestedEvent(any(CreateUserDTO.class));
  }

  @ParameterizedTest
  @MethodSource("invalidRoleAssociations")
  void shouldRejectUpdateWhenRoleHasNoRequiredAssociation(
      String role, String expectedMessage) throws Exception {

    UserDTO user = new UserDTO();
    user.setName("foo");
    user.setRoles(Set.of(role));

    String requestBody = jacksonMessageConverter.getObjectMapper()
        .writeValueAsString(user);

    mockMvc.perform(put("/api/users/foo")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isInternalServerError())
        .andExpect(content().string(
            "\"" + expectedMessage + "\""));

    verify(mockFacade, never())
        .updateSingleUser(any(UserDTO.class));
  }

  @ParameterizedTest
  @MethodSource("validRoleAssociations")
  void shouldCreateUserWhenRoleHasRequiredAssociation(
      String role,
      String trust,
      String programme) throws Exception {

    CreateUserDTO user = new CreateUserDTO();
    user.setName("newuser");
    user.setEmailAddress("newuser@nhs.net");
    user.setRoles(Set.of(role));

    if (trust != null) {
      user.setAssociatedTrusts(Set.of(trust));
    }

    if (programme != null) {
      user.setAssociatedProgrammes(Set.of(programme));
    }

    String requestBody = jacksonMessageConverter.getObjectMapper()
        .writeValueAsString(user);

    mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isAccepted());

    verify(mockFacade)
        .publishUserCreationRequestedEvent(createCaptor.capture());

    CreateUserDTO actual = createCaptor.getValue();

    assertThat(actual.getRoles().contains(role), is(true));

    if (trust != null) {
      assertThat(actual.getAssociatedTrusts(), hasItem(trust));
    }

    if (programme != null) {
      assertThat(actual.getAssociatedProgrammes(), hasItem(programme));
    }
  }

  @ParameterizedTest
  @MethodSource("validRoleAssociations")
  void shouldUpdateUserWhenRoleHasRequiredAssociation(
      String role, String trust,
      String programme) throws Exception {

    UserDTO user = new UserDTO();
    user.setAuthId("123");
    user.setName("foo");
    user.setRoles(Set.of(role));

    if (trust != null) {
      user.setAssociatedTrusts(Set.of(trust));
    }

    if (programme != null) {
      user.setAssociatedProgrammes(Set.of(programme));
    }

    String requestBody = jacksonMessageConverter.getObjectMapper()
        .writeValueAsString(user);

    mockMvc.perform(put("/api/users/foo")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isAccepted());

    verify(mockFacade).updateSingleUser(userCaptor.capture());

    UserDTO actual = userCaptor.getValue();

    assertThat(actual.getRoles(), hasItem(role));

    if (trust != null) {
      assertThat(actual.getAssociatedTrusts(), hasItem(trust));
    }

    if (programme != null) {
      assertThat(actual.getAssociatedProgrammes(), hasItem(programme));
    }
  }

  private static Stream<Arguments> invalidRoleAssociations() {
    return Stream.of(
        Arguments.of(TRUST_OBSERVER, TRUST_REQUIRED_MESSAGE),
        Arguments.of(TRUST_ADMIN, TRUST_REQUIRED_MESSAGE),
        Arguments.of(PROGRAMME_OBSERVER, PROGRAMME_REQUIRED_MESSAGE),
        Arguments.of(PROGRAMME_ADMIN, PROGRAMME_REQUIRED_MESSAGE)
    );
  }

  private static Stream<Arguments> validRoleAssociations() {
    return Stream.of(
        Arguments.of(TRUST_OBSERVER, TRUST_A, null),
        Arguments.of(TRUST_ADMIN, TRUST_A, null),
        Arguments.of(PROGRAMME_OBSERVER, null, PROGRAMME_A),
        Arguments.of(PROGRAMME_ADMIN, null, PROGRAMME_A)
    );
  }
}
