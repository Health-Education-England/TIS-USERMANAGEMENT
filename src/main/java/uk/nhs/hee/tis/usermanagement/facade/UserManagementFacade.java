package uk.nhs.hee.tis.usermanagement.facade;

import com.transform.hee.tis.keycloak.User;
import com.transformuk.hee.tis.profile.client.service.impl.CustomPageable;
import com.transformuk.hee.tis.profile.service.dto.HeeUserDTO;
import com.transformuk.hee.tis.reference.api.dto.DBCDTO;
import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import uk.nhs.hee.tis.usermanagement.DTOs.UserDTO;
import uk.nhs.hee.tis.usermanagement.exception.UpdateUserException;
import uk.nhs.hee.tis.usermanagement.exception.UserNotFoundException;
import uk.nhs.hee.tis.usermanagement.mapper.HeeUserMapper;
import uk.nhs.hee.tis.usermanagement.service.KeyCloakAdminClientService;
import uk.nhs.hee.tis.usermanagement.service.ProfileService;
import uk.nhs.hee.tis.usermanagement.service.ReferenceService;

import java.util.ArrayList;
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
    Optional<HeeUserDTO> optionalHeeUserDTO = profileService.getUserByUsername(username);
    Optional<User> optionalKeycloakUser = keyCloakAdminClientService.getUser(username);

    HeeUserDTO heeUserDTO = optionalHeeUserDTO.orElseThrow(() -> new UserNotFoundException(username, "Profile"));
    User kcUser = optionalKeycloakUser.orElseThrow(() -> new UserNotFoundException(username, "KC"));
    Set<DBCDTO> dbcdtos = referenceService.getAllDBCs();
    UserDTO completeUserDto = heeUserMapper.convert(heeUserDTO, kcUser, dbcdtos);

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
    Optional<User> optionalOriginalUser = keyCloakAdminClientService.getUser(userDTO.getName());
    User originalUser = optionalOriginalUser.orElseThrow(() -> new UserNotFoundException(userDTO.getName(), "KC"));
    boolean success = keyCloakAdminClientService.updateUser(userDTO);
    if (success) {
      Optional<HeeUserDTO> optionalHeeUserDTO = profileService.updateUser(heeUserMapper.convert(userDTO));
      if (!optionalHeeUserDTO.isPresent()) {
        //revert KC changes
        keyCloakAdminClientService.updateUser(originalUser);
      }
    } else {
      throw new UpdateUserException(userDTO.getName(), "KC");
    }
  }

  public List<String> getAllRoles() {
    return profileService.getAllRoles();
  }

  public List<DBCDTO> getAllDBCs() {
    return new ArrayList<>(referenceService.getAllDBCs());
  }

  public List<TrustDTO> getAllTrusts() {
    return new ArrayList<>(referenceService.getAllTrusts());
  }
}
