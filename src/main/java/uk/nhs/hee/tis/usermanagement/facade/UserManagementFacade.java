package uk.nhs.hee.tis.usermanagement.facade;

import com.transform.hee.tis.keycloak.User;
import com.transformuk.hee.tis.profile.client.service.impl.CustomPageable;
import com.transformuk.hee.tis.profile.service.dto.HeeUserDTO;
import com.transformuk.hee.tis.reference.api.dto.DBCDTO;
import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import uk.nhs.hee.tis.usermanagement.DTOs.CreateUserDTO;
import uk.nhs.hee.tis.usermanagement.DTOs.UserDTO;
import uk.nhs.hee.tis.usermanagement.DTOs.UserPasswordDTO;
import uk.nhs.hee.tis.usermanagement.event.CreateKeycloakUserRequestedEvent;
import uk.nhs.hee.tis.usermanagement.event.DeleteKeycloakUserRequestedEvent;
import uk.nhs.hee.tis.usermanagement.exception.UpdateUserException;
import uk.nhs.hee.tis.usermanagement.exception.UserNotFoundException;
import uk.nhs.hee.tis.usermanagement.mapper.HeeUserMapper;
import uk.nhs.hee.tis.usermanagement.service.KeyCloakAdminClientService;
import uk.nhs.hee.tis.usermanagement.service.ProfileService;
import uk.nhs.hee.tis.usermanagement.service.ReferenceService;
import uk.nhs.hee.tis.usermanagement.service.TcsService;

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

  @Autowired
  private ApplicationEventPublisher applicationEventPublisher;

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
      Optional<HeeUserDTO> optionalHeeUserDTO = profileService.updateUser(heeUserMapper.convert(userDTO, referenceService.getAllTrusts(), tcsService.getAllProgrammes()));
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
   * Publish an event that will kick off the creation of a user
   */
  public void publishUserCreationRequestedEvent(CreateUserDTO userDTO) {
    HeeUserDTO userToCreateInProfileService = heeUserMapper.convert(userDTO, referenceService.getAllTrusts(), tcsService.getAllProgrammes());
    applicationEventPublisher.publishEvent(new CreateKeycloakUserRequestedEvent(userDTO, userToCreateInProfileService));
  }

  public void publishDeleteKeycloakUserRequestedEvent(String username) {
    Optional<User> optionalUser = keyCloakAdminClientService.getUser(username);
    User kcUser = optionalUser.orElseThrow(() -> new UserNotFoundException(username, "Keycloak"));
    applicationEventPublisher.publishEvent(new DeleteKeycloakUserRequestedEvent(kcUser, true));
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

  public List<ProgrammeDTO> getAllProgrammes() {
    return new ArrayList<>(tcsService.getAllProgrammes());
  }

  public void updatePassword(UserPasswordDTO passwordDTO) {
    keyCloakAdminClientService.updatePassword(passwordDTO.getKcId(), passwordDTO.getPassword(), passwordDTO.isTempPassword());
  }
}
