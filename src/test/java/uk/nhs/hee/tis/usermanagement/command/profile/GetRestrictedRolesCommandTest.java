package uk.nhs.hee.tis.usermanagement.command.profile;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.transformuk.hee.tis.profile.client.service.ProfileService;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetRestrictedRolesCommandTest {

  private static final Set restrictedRoles = Set.of("Role1", "Role2", "Role3");

  @Mock
  private ProfileService service;

  @Test
  void testRunShouldReturnRestrictedRoles() {
    GetRestrictedRolesCommand command = new GetRestrictedRolesCommand(service);

    when(service.getRestrictedRoles()).thenReturn(restrictedRoles);

    Set<String> result = command.run();
    assertThat(result, is(restrictedRoles));
  }

  @Test
  void testRunShouldThrowException() {
    GetRestrictedRolesCommand command = new GetRestrictedRolesCommand(service);

    String message = "testing exception";
    when(service.getRestrictedRoles()).thenThrow(new RuntimeException(message));

    Exception thrown = assertThrows(
        RuntimeException.class, command::run);

    assertEquals(message, thrown.getMessage());
  }
}
