package uk.nhs.hee.tis.usermanagement.facade;

import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import com.google.common.collect.Lists;
import uk.nhs.hee.tis.usermanagement.service.KeyCloakAdminClientService;
import uk.nhs.hee.tis.usermanagement.service.ProfileService;

@RunWith(MockitoJUnitRunner.class)
public class UserManagementFacadeTest {

  private String adminRole = "HEE TIS Admin", etlRole = "ETL", roRole = "RVOfficer", rvAdmin = "RVAdmin";
  @InjectMocks
  UserManagementFacade testClass;
  
  @Mock
  ProfileService profileService;
  
  @Mock
  KeyCloakAdminClientService keyCloakAdminClientService;

  @Test
  public void testGetAllAssignableRoles() {
    List mockRoles = Lists.newArrayList(adminRole, rvAdmin, roRole , etlRole);

    when(profileService.getAllRoles()).thenReturn(mockRoles);
    List<String> actual = testClass.getAllAssignableRoles();
    assertThat(actual, containsInAnyOrder(adminRole, rvAdmin, etlRole));
    verify(profileService).getAllRoles();
  }

}