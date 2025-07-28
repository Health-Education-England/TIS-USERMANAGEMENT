package uk.nhs.hee.tis.usermanagement.facade;

import com.transformuk.hee.tis.profile.client.service.impl.CustomPageable;
import com.transformuk.hee.tis.profile.dto.RoleDTO;
import com.transformuk.hee.tis.profile.service.dto.HeeUserDTO;
import com.transformuk.hee.tis.reference.api.dto.DBCDTO;
import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeDTO;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import uk.nhs.hee.tis.usermanagement.DTOs.AuthenticationUserDto;
import uk.nhs.hee.tis.usermanagement.DTOs.CreateUserDTO;
import uk.nhs.hee.tis.usermanagement.DTOs.UserAuthEventDto;
import uk.nhs.hee.tis.usermanagement.DTOs.UserDTO;
import uk.nhs.hee.tis.usermanagement.DTOs.UserPasswordDTO;
import uk.nhs.hee.tis.usermanagement.event.CreateAuthenticationUserRequestedEvent;
import uk.nhs.hee.tis.usermanagement.event.DeleteAuthenticationUserRequestedEvent;
import uk.nhs.hee.tis.usermanagement.exception.IdentityProviderException;
import uk.nhs.hee.tis.usermanagement.exception.UpdateUserException;
import uk.nhs.hee.tis.usermanagement.exception.UserNotFoundException;
import uk.nhs.hee.tis.usermanagement.mapper.HeeUserMapper;
import uk.nhs.hee.tis.usermanagement.service.AuthenticationAdminService;
import uk.nhs.hee.tis.usermanagement.service.ProfileService;
import uk.nhs.hee.tis.usermanagement.service.ReferenceService;
import uk.nhs.hee.tis.usermanagement.service.TcsService;
import uk.nhs.hee.tis.usermanagement.util.PasswordUtil;

@Component
public class UserManagementFacade {

  private static final Logger LOG = LoggerFactory.getLogger(UserManagementFacade.class);

  private static final Collection<String> entities = Collections.singleton("HEE");

  private ProfileService profileService;

  private TcsService tcsService;

  private AuthenticationAdminService authenticationAdminService;

  private HeeUserMapper heeUserMapper;

  private ReferenceService referenceService;

  private ApplicationEventPublisher applicationEventPublisher;

  private PasswordUtil passwordUtil;

  public UserManagementFacade(ProfileService profileService, TcsService tcsService,
      AuthenticationAdminService authenticationAdminService, HeeUserMapper heeUserMapper,
      ReferenceService referenceService, ApplicationEventPublisher applicationEventPublisher,
      PasswordUtil passwordUtil) {
    this.profileService = profileService;
    this.tcsService = tcsService;
    this.authenticationAdminService = authenticationAdminService;
    this.heeUserMapper = heeUserMapper;
    this.referenceService = referenceService;
    this.applicationEventPublisher = applicationEventPublisher;
    this.passwordUtil = passwordUtil;
  }

  public UserDTO getCompleteUser(String username) {
    Optional<HeeUserDTO> optionalHeeUserDTO = profileService.getUserByUsername(username);
    Optional<AuthenticationUserDto> optionalAuthenticationUser = authenticationAdminService.getUser(
        username);

    HeeUserDTO heeUserDTO = optionalHeeUserDTO.orElseThrow(
        () -> new UserNotFoundException(username, ProfileService.NAME));
    AuthenticationUserDto authenticationUser = optionalAuthenticationUser.orElseThrow(
        () -> new UserNotFoundException(username, authenticationAdminService.getServiceName()));

    // Filter out restricted roles from the user
    Set<String> restrictedRoles = profileService.getRestrictedRoles();
    Set<RoleDTO> heeUserRoleDtos = heeUserDTO.getRoles();
    if (heeUserRoleDtos != null && restrictedRoles != null) {
      heeUserRoleDtos.removeIf(roleDto -> restrictedRoles.contains(roleDto.getName()));
    }

    return heeUserMapper.convert(heeUserDTO, authenticationUser);
  }

  public UserDTO getUserByNameIgnoreCase(String username) {
    Optional<HeeUserDTO> optionalHeeUserDTO = profileService.getUserByUsernameIgnoreCase(username);
    if (optionalHeeUserDTO.isEmpty()) {
      return null;
    } else {
      HeeUserDTO heeUserDTO = optionalHeeUserDTO.get();
      return heeUserMapper.convert(heeUserDTO);
    }
  }

  public Page<UserDTO> getAllUsers(Pageable pageable, String search) {
    Page<HeeUserDTO> heeUserDTOS = profileService.getAllUsers(pageable, search);
    List<UserDTO> userDTOS = new ArrayList<>();
    heeUserDTOS.getContent().stream().forEach(profileUser ->
        authenticationAdminService.getUser(profileUser.getName())
            .ifPresentOrElse(authUser -> userDTOS.add(heeUserMapper.convert(profileUser, authUser)),
                () -> userDTOS.add(heeUserMapper.convert(profileUser))));

    return new CustomPageable<>(userDTOS, pageable, heeUserDTOS.getTotalElements());
  }

  /**
   * Update a user in both KC and the Profile service.
   * <p>
   * Do KC first as thats more likely to fail
   *
   * @param userDto The complete information to be updated across backend services
   */
  public void updateSingleUser(UserDTO userDto) {
    Optional<AuthenticationUserDto> optionalOriginalUser = authenticationAdminService.getUser(
        userDto.getName());
    AuthenticationUserDto originalUser = optionalOriginalUser.orElseThrow(
        () -> new UserNotFoundException(userDto.getName(),
            authenticationAdminService.getServiceName()));

    Optional<HeeUserDTO> optionalOriginalHeeUser = profileService.getUserByUsername(
        userDto.getName());
    final Set<String> restrictedRoles = profileService.getRestrictedRoles();
    boolean success = authenticationAdminService.updateUser(userDto);
    if (success) {
      HeeUserDTO originalHeeUser = optionalOriginalHeeUser.orElseThrow(
          () -> new UserNotFoundException(userDto.getName(), ProfileService.NAME));
      originalHeeUser.getRoles().stream()
          .filter(r -> restrictedRoles.contains(r.getName()))
          .forEach(r -> userDto.getRoles().add(r.getName()));
      Optional<HeeUserDTO> optionalHeeUserDTO = profileService.updateUser(
          heeUserMapper.convert(userDto, referenceService.getAllTrusts(),
              tcsService.getAllProgrammes()));
      if (optionalHeeUserDTO.isEmpty()) {
        //revert KC changes
        if (!authenticationAdminService.updateUser(originalUser)) {
          LOG.error(
              "Could not revert KC changes back to previous version after profile update failed! Its possible that KC user [{}] is out of sync",
              originalUser.getUsername());
        }
        throw new UpdateUserException(userDto.getName(), ProfileService.NAME);
      }
    } else {
      throw new UpdateUserException(userDto.getName(), authenticationAdminService.getServiceName());
    }
  }

  /**
   * Publish an event that will kick off the creation of a user.
   */
  public void publishUserCreationRequestedEvent(CreateUserDTO userDto) {
    HeeUserDTO userToCreateInProfileService = heeUserMapper.convert(userDto,
        referenceService.getAllTrusts(), tcsService.getAllProgrammes());
    applicationEventPublisher.publishEvent(
        new CreateAuthenticationUserRequestedEvent(userDto, userToCreateInProfileService));
  }

  /**
   * Publish an event that will kick off the deletion of a user.
   *
   * @param username The unique preferred username / name of the user to delete
   */
  public void publishDeleteAuthenticationUserRequestedEvent(String username) {
    Optional<AuthenticationUserDto> optionalUser = authenticationAdminService.getUser(username);
    AuthenticationUserDto user = optionalUser.orElseThrow(
        () -> new UserNotFoundException(username, authenticationAdminService.getServiceName()));
    applicationEventPublisher.publishEvent(new DeleteAuthenticationUserRequestedEvent(user, true));
  }

  /**
   * Get a list of roles managed via the web user interface
   *
   * @return roles - A list of roles that can be assigned from the web application
   */
  public List<String> getAllAssignableRoles() {
    return profileService.getAllAssignableRoles();
  }

  public List<String> getAllEntityRoles() {
    return new ArrayList<>(entities);
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

  /**
   * Update the user password.
   *
   * @param passwordDto The DTO containing data about the user and their new password.
   */
  public void updatePassword(UserPasswordDTO passwordDto) {
    authenticationAdminService.updatePassword(passwordDto.getKcId(), passwordDto.getPassword(),
        passwordDto.isTempPassword());
  }

  /**
   * Get authentication event logs for user.
   *
   * @param username The username to get the auth event logs for.
   */
  public List<UserAuthEventDto> getUserAuthEvents(String username) {
    try {
      return authenticationAdminService.getUserAuthEvents(username);
    } catch (Exception e) {
      throw new IdentityProviderException(e.getMessage(), e);
    }
  }

  /**
   * Trigger password reset for a user.
   *
   * @param username the username to be password reset triggered
   * @return the updated temporary password
   */
  public String triggerPasswordReset(String username) {
    String tempPassword = passwordUtil.generatePassword();
    authenticationAdminService.updatePassword(username, tempPassword, true);
    return tempPassword;
  }
}
