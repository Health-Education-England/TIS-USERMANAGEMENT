package uk.nhs.hee.tis.usermanagement.facade;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import com.transformuk.hee.tis.profile.service.dto.HeeUserDTO;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import uk.nhs.hee.tis.usermanagement.DTOs.UserDTO;
import uk.nhs.hee.tis.usermanagement.mapper.HeeUserMapper;
import uk.nhs.hee.tis.usermanagement.service.ProfileService;

@RunWith(MockitoJUnitRunner.class)
public class UserManagementFacadeTest {

  private String adminRole = "HEE TIS Admin", etlRole = "ETL", roRole = "RVOfficer", rvAdmin = "RVAdmin";
  private String heeEntity = "HEE", niEntity = "NI";
  @InjectMocks
  UserManagementFacade testClass;

  @Mock
  ProfileService profileService;

  @Spy
  private HeeUserMapper userMapper;

  @Test
  public void testGetAllAssignableRoles() {
    List mockRoles = Lists.newArrayList(adminRole, rvAdmin, roRole, etlRole);

    when(profileService.getAllRoles()).thenReturn(mockRoles);
    List<String> actual = testClass.getAllAssignableRoles();
    assertThat(actual, containsInAnyOrder(adminRole, rvAdmin, etlRole));
    verify(profileService).getAllRoles();
  }

  /**
   * Test that null is returned when the user is not found.
   */
  @Test
  public void testGetUserByNameIgnoreCase_userNotFound_null() {
    // Record expectations.
    when(profileService.getUserByUsernameIgnoreCase("testUser_1")).thenReturn(Optional.empty());

    // Call the code under test.
    UserDTO user = testClass.getUserByNameIgnoreCase("testUser_1");

    // Perform assertions.
    assertThat("The user did not match the expected value.", user, nullValue());
  }

  /**
   * Test that the user is returned when the user is found.
   */
  @Test
  public void testGetUserByNameIgnoreCase_userFound_user() {
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
  public void testGetAllEntities() {
    List mockEntities = Lists.newArrayList(heeEntity, niEntity);

    when(profileService.getAllEntities()).thenReturn(mockEntities);
    List<String> actual = testClass.getAllEntities();
    assertThat(actual, containsInAnyOrder(heeEntity, niEntity));
    verify(profileService).getAllEntities();
  }
}
