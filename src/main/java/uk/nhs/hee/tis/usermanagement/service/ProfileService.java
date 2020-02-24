package uk.nhs.hee.tis.usermanagement.service;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.transformuk.hee.tis.profile.client.service.impl.ProfileServiceImpl;
import com.transformuk.hee.tis.profile.service.dto.HeeUserDTO;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.nhs.hee.tis.usermanagement.command.profile.*;
import uk.nhs.hee.tis.usermanagement.event.CreateProfileUserRequestedEvent;
import uk.nhs.hee.tis.usermanagement.event.DeleteKeycloakUserRequestedEvent;
import uk.nhs.hee.tis.usermanagement.event.DeleteProfileUserRequestEvent;

import java.util.List;
import java.util.Optional;

@Service
public class ProfileService {

  private static final Logger LOG = LoggerFactory.getLogger(ProfileService.class);
  protected static final String HEE_USERS_ENDPOINT = "/api/hee-users";

  public static final String NAME = "Profile";

  @Autowired
  private ProfileServiceImpl profileServiceImpl;
  @Autowired
  private RestTemplate profileRestTemplate;
  @Autowired
  private ApplicationEventPublisher applicationEventPublisher;
  @Value("${profile.service.url}")
  private String serviceUrl;

  /**
   * Get all users in a paginated form. If a username is provideded, a (sql) like search is done.
   *
   * @param pageable pageable object defining the the page as well a size required
   * @param username nullable username, if provided does the like search, if null then empty search
   * @return Page of HeeUserDTO
   */
  public Page<HeeUserDTO> getAllUsers(Pageable pageable, @Nullable String username) {
    Preconditions.checkNotNull(pageable, "Pageable cannot be null when requesting a paginated result");

    GetPaginatedUsersCommand getPaginatedUsersCommand = new GetPaginatedUsersCommand(profileServiceImpl, pageable, username);
    return getPaginatedUsersCommand.execute();
  }

  /**
   * Get a single user by username
   *
   * @param username the username to search by
   * @return an optional of the found user
   */
  public Optional<HeeUserDTO> getUserByUsername(String username) {
    Preconditions.checkNotNull(username, "Username cannot be null when searching for a user by username");

    GetUserByUsernameCommand getUserByUsernameCommand = new GetUserByUsernameCommand(profileServiceImpl, username, false);
    return getUserByUsernameCommand.execute();
  }

  public Optional<HeeUserDTO> getUserByUsernameIgnoreCase(String username) {
    Preconditions.checkNotNull(username, "Username cannot be null when searching for a user by username");

    GetUserByUsernameCommand getUserByUsernameCommand = new GetUserByUsernameCommand(profileServiceImpl, username, true);
    return getUserByUsernameCommand.execute();
  }

  /**
   * Create a user in the profile service
   *
   * @param event the new user details
   * @return an optional HeeUserDTO that represents the user in the profile service
   */
  @EventListener
  public void createProfileUserEventListener(CreateProfileUserRequestedEvent event) {
    Preconditions.checkNotNull(event.getHeeUserDTO(), "Cannot create user if user is null");
    try {
      LOG.info("Received CreateProfileUserEvent for user [{}]", event.getHeeUserDTO().getEmailAddress());
      profileServiceImpl.createDto(event.getHeeUserDTO(), HEE_USERS_ENDPOINT, HeeUserDTO.class);
      LOG.info("Create complete for user [{}]", event.getHeeUserDTO().getEmailAddress());
    } catch (Exception e) {
      LOG.info("Error occurred while creating user in Profile service with the following data: [{}]", new Gson().toJson(event.getHeeUserDTO()));
      LOG.info(ExceptionUtils.getStackTrace(e));
      // reverse call to kc
      LOG.info("Publishing event to reverse the kc user create for user [{}]", event.getKcUser().getEmail());
      applicationEventPublisher.publishEvent(new DeleteKeycloakUserRequestedEvent(event.getKcUser(), false));
    }
  }

  /**
   * Update a user in the profile service
   *
   * @param userToUpdateDTO the state of the user details to update to
   * @return an optional of the HeeUserDTO that represents the user in the profile service
   */
  public Optional<HeeUserDTO> updateUser(HeeUserDTO userToUpdateDTO) {
    Preconditions.checkNotNull(userToUpdateDTO, "Cannot update user if user to update is null");

    UpdateUserCommand updateUserCommand = new UpdateUserCommand(profileServiceImpl, userToUpdateDTO);
    return updateUserCommand.execute();
  }

  /**
   * YOLO
   *
   * @return
   */
  public List<String> getAllRoles() {
    GetAllRolesCommand getAllRolesCommand = new GetAllRolesCommand(profileRestTemplate, serviceUrl);
    return getAllRolesCommand.execute();
  }

  public List<String> getAllEntities() {
    GetAllEntitiesCommand getAllRolesCommand = new GetAllEntitiesCommand(profileRestTemplate, serviceUrl);
    return getAllRolesCommand.execute();
  }

  @EventListener
  public void deleteUserEventListener(DeleteProfileUserRequestEvent event) {
    LOG.info("Received DeleteProfileUserEvent for user [{}]", event.getUsername());
    profileServiceImpl.deleteUser(event.getUsername());
  }


}
