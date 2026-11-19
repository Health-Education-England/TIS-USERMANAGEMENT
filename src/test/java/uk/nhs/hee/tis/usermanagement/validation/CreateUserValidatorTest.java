package uk.nhs.hee.tis.usermanagement.validation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.hee.tis.usermanagement.DTOs.CreateUserDTO;
import uk.nhs.hee.tis.usermanagement.DTOs.UserDTO;
import uk.nhs.hee.tis.usermanagement.exception.UserCreationException;
import uk.nhs.hee.tis.usermanagement.facade.UserManagementFacade;

@ExtendWith(MockitoExtension.class)
class CreateUserValidatorTest {

  @Mock
  private UserManagementFacade userManagementFacade;

  private CreateUserValidator createUserValidator;

  @BeforeEach
  void setUp() {
    createUserValidator = new CreateUserValidator(userManagementFacade);
  }

  @ParameterizedTest
  @MethodSource("invalidNameAndEmailInputs")
  void shouldThrowWhenNameOrEmailContainsWhitespace(String username, String email, String message) {
    CreateUserDTO user = new CreateUserDTO();
    user.setName(username);
    user.setEmailAddress(email);

    UserCreationException exception =
        assertThrows(UserCreationException.class,
            () -> createUserValidator.validateNameAndEmail(user));

    assertEquals(message, exception.getMessage());
  }

  @Test
  void shouldThrowWhenUsernameAlreadyExists() {
    String username = "existing.user";
    when(userManagementFacade.getUserByNameIgnoreCase(username)).thenReturn(new UserDTO());

    UserCreationException exception = assertThrows(UserCreationException.class,
        () -> createUserValidator.validateUsernameAvailable(username));

    assertEquals("Cannot create user, the username already exists", exception.getMessage());
  }

  @Test
  void shouldNotThrowWhenUsernameIsAvailable() {
    String username = "new.user";
    when(userManagementFacade.getUserByNameIgnoreCase(username)).thenReturn(null);

    assertDoesNotThrow(() -> createUserValidator.validateUsernameAvailable(username));
  }

  private static Stream<Arguments> invalidNameAndEmailInputs() {
    return Stream.of(
        Arguments.of("bad user", "valid@tis.nhs.uk",
            "Cannot create user, username shouldn't contain white spaces"),
        Arguments.of("valid.user", "bad email@tis.nhs.uk",
            "Cannot create user, email address shouldn't contain white spaces")
    );
  }
}

