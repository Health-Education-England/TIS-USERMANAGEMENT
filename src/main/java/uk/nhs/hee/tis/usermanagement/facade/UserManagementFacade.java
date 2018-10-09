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
    Optional<HeeUserDTO> heeUserDTO = profileService.getUserByUsername(username);
    UserDTO completeUserDto = new UserDTO();
    if (heeUserDTO.isPresent()) {
      completeUserDto = heeUserMapper.mapHeeUserAttributes(completeUserDto, heeUserDTO.get());
      Optional<User> optionalKeycloakUser = keyCloakAdminClientService.getUser(username);
      if (optionalKeycloakUser.isPresent()) {
        User kcUser = optionalKeycloakUser.get();
        completeUserDto = heeUserMapper.mapKeycloakAttributes(completeUserDto, kcUser);
      }
      Set<DBCDTO> dbcdtos = referenceService.getAllDBCs();
      completeUserDto = heeUserMapper.mapDBCAttributes(completeUserDto, heeUserDTO.get(), dbcdtos);

    }
    return Optional.ofNullable(completeUserDto);
  }

  public Page<UserDTO> getAllUsers(Pageable pageable, String search) {
    Page<HeeUserDTO> heeUserDTOS = profileService.getAllUsers(pageable, search);
    List<UserDTO> userDTOS = heeUserMapper.convertAll(heeUserDTOS.getContent());
    return new CustomPageable<>(userDTOS, pageable, heeUserDTOS.getTotalElements());
  }

  /**
   * Update a user in both KC and the Profile service.
   * <p>
   * Do KC first as thats more likely to fail
   *
   * @param userDTO
   */
  public void updateSingleUser(UserDTO userDTO) {
    Optional<User> originalUser = keyCloakAdminClientService.getUser(userDTO.getName());
    if (originalUser.isPresent()) {
      boolean success = keyCloakAdminClientService.updateUser(userDTO);
      if (success) {
        Optional<HeeUserDTO> optionalHeeUserDTO = profileService.updateUser(heeUserMapper.convert(userDTO));
        if (!optionalHeeUserDTO.isPresent()) {
          //revert KC changes
          keyCloakAdminClientService.updateUser(originalUser.get());
        }
      }
    }
  }
}
