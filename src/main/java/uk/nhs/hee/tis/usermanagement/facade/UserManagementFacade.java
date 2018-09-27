package uk.nhs.hee.tis.usermanagement.facade;

import com.transform.hee.tis.keycloak.User;
import com.transformuk.hee.tis.profile.client.service.impl.CustomPageable;
import com.transformuk.hee.tis.profile.service.dto.HeeUserDTO;
import com.transformuk.hee.tis.reference.api.dto.DBCDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import uk.nhs.hee.tis.usermanagement.DTOs.UserDTO;
import uk.nhs.hee.tis.usermanagement.mapper.HeeUserMapper;
import uk.nhs.hee.tis.usermanagement.service.KeyCloakAdminClientService;
import uk.nhs.hee.tis.usermanagement.service.ProfileService;
import uk.nhs.hee.tis.usermanagement.service.ReferenceService;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class UserManagementFacade {

  @Autowired
  ProfileService profileService;

  @Autowired
  KeyCloakAdminClientService keyCloakAdminClientService;

  @Autowired
  private HeeUserMapper heeUserMapper;

  @Autowired
  private ReferenceService referenceService;

  public Optional<UserDTO> getCompleteUser(String username) {
    HeeUserDTO heeUserDTO = profileService.getUserByUsername(username);
    User kcUser = keyCloakAdminClientService.getUser(username);
    Set<DBCDTO> dbcdtos = referenceService.getAllDBCs();
    UserDTO completeUserDto = heeUserMapper.convert(heeUserDTO, kcUser, dbcdtos);
    return Optional.of(completeUserDto);
  }

  public Page<UserDTO> getAllUsers(Pageable pageable, String search) {
    Page<HeeUserDTO> heeUserDTOS = profileService.getAllUsers(pageable, search);
    List<UserDTO> userDTOS = heeUserMapper.convertAll(heeUserDTOS.getContent());
    return new CustomPageable<>(userDTOS, pageable, heeUserDTOS.getTotalElements());
  }
}
