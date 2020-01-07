package uk.nhs.hee.tis.usermanagement.command.profile;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import com.transformuk.hee.tis.profile.client.service.ProfileService;
import com.transformuk.hee.tis.profile.service.dto.HeeUserDTO;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The unit tests for {@link GetUserByUsernameCommand}.
 */
@RunWith(MockitoJUnitRunner.class)
public class GetUserByUsernameCommandTest {

  @Mock
  private ProfileService service;

  /**
   * Test that an empty optional is returned when ignore case is false and there is no matching
   * user.
   */
  @Test
  public void testRun_notIgnoreCaseNoMatchingUser_empty() {
    // Set up test data.
    GetUserByUsernameCommand command = new GetUserByUsernameCommand(service, "testUser_1", false);

    // Record expectations.
    when(service.getSingleAdminUser("testUser_1")).thenReturn(null);

    // Call the code under test.
    Optional<HeeUserDTO> userOptional = command.run();

    // Perform assertions.
    assertThat("The user did not match the expected value.", userOptional, is(Optional.empty()));
  }

  /**
   * Test that a single admin user is returned when ignore case is false and there is a matching
   * user.
   */
  @Test
  public void testRun_notIgnoreCaseMatchingUser_adminUser() {
    // Set up test data.
    GetUserByUsernameCommand command = new GetUserByUsernameCommand(service, "testUser_1", false);

    HeeUserDTO user = new HeeUserDTO();
    user.setName("testUser_1");

    // Record expectations.
    when(service.getSingleAdminUser("testUser_1")).thenReturn(user);

    // Call the code under test.
    Optional<HeeUserDTO> userOptional = command.run();

    // Perform assertions.
    assertThat("The user did not match the expected value.", userOptional, is(Optional.of(user)));
  }


  /**
   * Test that an empty optional is returned when ignore case is true and there are no matching
   * users.
   */
  @Test
  public void testRun_ignoreCaseNoUsers_empty() {
    // Set up test data.
    GetUserByUsernameCommand command = new GetUserByUsernameCommand(service, "testUser_1", true);

    // Record expectations.
    when(service.getUsersByNameIgnoreCase("testUser_1")).thenReturn(Collections.emptyList());

    // Call the code under test.
    Optional<HeeUserDTO> userOptional = command.run();

    // Perform assertions.
    assertThat("The user did not match the expected value.", userOptional, is(Optional.empty()));
  }


  /**
   * Test that a single user is retrieved when ignore case is true and there are matching users.
   */
  @Test
  public void testRun_ignoreCaseHasUsers_firstUser() {
    // Set up test data.
    GetUserByUsernameCommand command = new GetUserByUsernameCommand(service, "testUser_1", true);

    HeeUserDTO user1 = new HeeUserDTO();
    user1.setName("testUser_1");
    HeeUserDTO user2 = new HeeUserDTO();
    user2.setName("testUser_1");

    // Record expectations.
    when(service.getUsersByNameIgnoreCase("testUser_1")).thenReturn(Arrays.asList(user1, user2));

    // Call the code under test.
    Optional<HeeUserDTO> userOptional = command.run();

    // Perform assertions.
    assertThat("The user did not match the expected value.", userOptional, is(Optional.of(user1)));
  }
}
