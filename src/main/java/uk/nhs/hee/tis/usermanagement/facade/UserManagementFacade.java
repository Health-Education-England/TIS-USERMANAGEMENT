package uk.nhs.hee.tis.usermanagement.facade;

import com.transform.hee.tis.keycloak.User;
import com.transformuk.hee.tis.profile.client.service.impl.CustomPageable;
import com.transformuk.hee.tis.profile.service.dto.HeeUserDTO;
import com.transformuk.hee.tis.reference.api.dto.DBCDTO;
import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import uk.nhs.hee.tis.usermanagement.DTOs.CreateUserDTO;
import uk.nhs.hee.tis.usermanagement.DTOs.UserDTO;
import uk.nhs.hee.tis.usermanagement.DTOs.UserPasswordDTO;
import uk.nhs.hee.tis.usermanagement.exception.UpdateUserException;
import uk.nhs.hee.tis.usermanagement.exception.UserCreationException;
import uk.nhs.hee.tis.usermanagement.exception.UserDeletionException;
import uk.nhs.hee.tis.usermanagement.exception.UserNotFoundException;
import uk.nhs.hee.tis.usermanagement.mapper.HeeUserMapper;
import uk.nhs.hee.tis.usermanagement.service.KeyCloakAdminClientService;
import uk.nhs.hee.tis.usermanagement.service.ProfileService;
import uk.nhs.hee.tis.usermanagement.service.ReferenceService;
import uk.nhs.hee.tis.usermanagement.service.TcsService;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class UserManagementFacade {

  private static final Logger LOG = LoggerFactory.getLogger(UserManagementFacade.class);

  @Autowired
  private ProfileService profileService;

  @Autowired
  private TcsService tcsService;

  @Autowired
  private KeyCloakAdminClientService keyCloakAdminClientService;

  @Autowired
  private HeeUserMapper heeUserMapper;

  @Autowired
  private ReferenceService referenceService;

  public UserDTO getCompleteUser(String username) {
    Optional<HeeUserDTO> optionalHeeUserDTO = profileService.getUserByUsername(username);
    Optional<User> optionalKeycloakUser = keyCloakAdminClientService.getUser(username);

    HeeUserDTO heeUserDTO = optionalHeeUserDTO.orElseThrow(() -> new UserNotFoundException(username, "Profile"));
    User kcUser = optionalKeycloakUser.orElseThrow(() -> new UserNotFoundException(username, "KC"));
    Set<DBCDTO> dbcdtos = referenceService.getAllDBCs();
    return heeUserMapper.convert(heeUserDTO, kcUser, dbcdtos);
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
      //                                                                                 user will be updated against the current trusts
      Optional<HeeUserDTO> optionalHeeUserDTO = profileService.updateUser(heeUserMapper.convert(userDTO, referenceService.getAllCurrentTrusts(), tcsService.getAllProgrammes() ));
      if (!optionalHeeUserDTO.isPresent()) {
        //revert KC changes
        if (!keyCloakAdminClientService.updateUser(originalUser)) {
          LOG.error("Could not revert KC changes back to previous version after profile update failed! Its possible that KC user [{}] is out of sync", userDTO.getName());
        }
        throw new UpdateUserException(userDTO.getName(), "Profile");
      }
    } else {
      throw new UpdateUserException(userDTO.getName(), "KC");
    }
  }

  /**
   * Create a user in both KC and Profile service.
   *
   * @param userDTO the DTO that contains the details to create
   */
  public void createUser(CreateUserDTO userDTO) {
    Optional<User> optionalKcUser = keyCloakAdminClientService.createUser(userDTO);
    User kcUser = optionalKcUser.orElseThrow(() -> new UserCreationException("Could not create user in KC"));
    //                                                                                 user will be created against the current trusts not any INACTIVE trusts
    Optional<HeeUserDTO> optionalHeeUserDTO = profileService.createUser(heeUserMapper.convert(userDTO, referenceService.getAllCurrentTrusts(), tcsService.getAllProgrammes()));
    if (!optionalHeeUserDTO.isPresent()) {
      LOG.warn("Attempting to revert creation of user in KC");
      if (!keyCloakAdminClientService.deleteUser(kcUser)) {
        LOG.error("Could not revert KC changes back to previous version create creating user in Profile failed. There may be more users in KC now than Profile user [{}]", userDTO.getName());
      }
      throw new UserCreationException("Could not create user " + userDTO.getName() + " in Profile service");
    }
  }

  public void deleteUser(String username) {
    Optional<User> optionalUser = keyCloakAdminClientService.getUser(username);
    User kcUser = optionalUser.orElseThrow(() -> new UserNotFoundException(username, "Keycloak"));
    boolean success = keyCloakAdminClientService.deleteUser(kcUser);
    if (success) {
      profileService.deleteUser(username);
    } else {
      throw new UserDeletionException(username);
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

  public List<TrustDTO> getAllCurrentTrusts(){
    return  new ArrayList<>(referenceService.getAllCurrentTrusts());
  }

  public List<ProgrammeDTO> getAllProgrammes() {
    return new ArrayList<>(tcsService.getAllProgrammes());
  }

  public void updatePassword(UserPasswordDTO passwordDTO) {
    keyCloakAdminClientService.updatePassword(passwordDTO.getKcId(), passwordDTO.getPassword(), passwordDTO.isTempPassword());
  }
}
