package uk.nhs.hee.tis.usermanagement.facade;

import com.transform.hee.tis.keycloak.KeycloakAdminClient;
import com.transformuk.hee.tis.profile.service.dto.HeeUserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import uk.nhs.hee.tis.usermanagement.DTOs.UserDTO;
import uk.nhs.hee.tis.usermanagement.mapper.HeeUserMapper;
import uk.nhs.hee.tis.usermanagement.service.KeyCloakAdminClientService;
import uk.nhs.hee.tis.usermanagement.service.ProfileService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class UserManagementFacade {

  @Autowired
  ProfileService profileService;

  @Autowired
  KeyCloakAdminClientService keyCloakAdminClientService;

  @Autowired
  private HeeUserMapper heeUserMapper;

  public Optional<UserDTO> getCompleteUser(String userName) {
    return Optional.empty();
  }

  public List<UserDTO> getAllUsers(Pageable pageable) {
    List<HeeUserDTO> heeUserDTOS = profileService.getAllUsers(pageable);
    return heeUserMapper.convertAll(heeUserDTOS);
  }
}
