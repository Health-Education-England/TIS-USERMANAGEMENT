package uk.nhs.hee.tis.usermanagement.service;

import com.google.common.base.Preconditions;
import com.transform.hee.tis.keycloak.KeycloakAdminClient;
import com.transform.hee.tis.keycloak.User;
import org.keycloak.representations.idm.GroupRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.nhs.hee.tis.usermanagement.DTOs.CreateUserDTO;
import uk.nhs.hee.tis.usermanagement.DTOs.UserDTO;
import uk.nhs.hee.tis.usermanagement.command.keycloak.GetUserAttributesCommand;
import uk.nhs.hee.tis.usermanagement.command.keycloak.GetUserCommand;
import uk.nhs.hee.tis.usermanagement.command.keycloak.GetUserGroupsCommand;
import uk.nhs.hee.tis.usermanagement.command.keycloak.UpdateUserCommand;
import uk.nhs.hee.tis.usermanagement.event.CreateKeycloakUserRequestedEvent;
import uk.nhs.hee.tis.usermanagement.event.CreateProfileUserRequestedEvent;
import uk.nhs.hee.tis.usermanagement.event.DeleteKeycloakUserRequestedEvent;
import uk.nhs.hee.tis.usermanagement.event.DeleteProfileUserRequestEvent;
import uk.nhs.hee.tis.usermanagement.exception.PasswordException;
import uk.nhs.hee.tis.usermanagement.exception.UserNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class KeyCloakAdminClientService {
  
  static final String REALM_LIN = "lin";

  private static final Logger LOG = LoggerFactory.getLogger(KeyCloakAdminClientService.class);

  public static final String NAME = "Keycloak";

  @Autowired
  private KeycloakAdminClient keycloakAdminClient;

  @Autowired
  private ApplicationEventPublisher applicationEventPublisher;

  public Optional<User> getUser(String username) {
    Preconditions.checkNotNull(username, "Cannot get user if username is null");

    GetUserCommand getUserCommand = new GetUserCommand(keycloakAdminClient, username, REALM_LIN);
    return getUserCommand.execute();
  }

  /**
   * Create user in Keycloak and
   *
   * @param event the user to create
   */
  @EventListener
  public void createUserEventListener(CreateKeycloakUserRequestedEvent event) {
    // create user in KeyCloak
    LOG.info("Received CreateKeycloakUserEvent for user [{}]", event.getUserDTO().getEmailAddress());
    User userToCreate = heeUserToKeycloakUser(event.getUserDTO());
    User kcUser = keycloakAdminClient.createUser(REALM_LIN, userToCreate);
    applicationEventPublisher.publishEvent(new CreateProfileUserRequestedEvent(event.getUserToCreateInProfileService(), kcUser));
  }


  /**
   * Update user in Keycloak
   *
   * @param userDTO
   */
  public boolean updateUser(UserDTO userDTO) {
    Preconditions.checkNotNull(userDTO, "Cannot update user in KC if the user is null");

    // Need to validate here - or check KC client behaviour
    Optional<User> existingUser = getUser(userDTO.getName());
    existingUser.orElseThrow(() -> new UserNotFoundException(userDTO.getName(), "keycloak"));

    User userToUpdate = heeUserToKeycloakUser(userDTO);
    return updateUser(userToUpdate);
  }

  public boolean updateUser(User user) {
    Preconditions.checkNotNull(user, "cannot update user if user is null");

    UpdateUserCommand updateUserCommand = new UpdateUserCommand(keycloakAdminClient, REALM_LIN, user.getId(), user);
    return updateUserCommand.execute();
  }

  public boolean updatePassword(String userId, String password, boolean tempPassword) {
    Preconditions.checkNotNull(userId);
    Preconditions.checkNotNull(password);

    boolean success = keycloakAdminClient.updateUserPassword(REALM_LIN, userId, password, tempPassword);
    if (!success) {
      throw new PasswordException("Update password with KC failed");
    }
    return success;
  }

  /**
   * Get the user attributes attached to a keycloak user
   *
   * @param username the username of the kc user
   * @return Map of keys to list of strings
   */
  public Map<String, List<String>> getUserAttributes(String username) {
    Preconditions.checkNotNull(username, "Cannot get attributes of user is username is null");

    GetUserAttributesCommand getUserAttributesCommand = new GetUserAttributesCommand(keycloakAdminClient, REALM_LIN, username);
    return getUserAttributesCommand.execute();
  }

  /**
   * Get a list of groups assigned to the user
   *
   * @param username the username of the kc user
   * @return List of groups
   */
  public List<GroupRepresentation> getUserGroups(String username) {
    Preconditions.checkNotNull(username, "Cannot get groups of user if username is null");

    Optional<User> optionalUser = getUser(username);
    User user = optionalUser.orElseThrow(() -> new UserNotFoundException(username, NAME));

    GetUserGroupsCommand getUserGroupsCommand = new GetUserGroupsCommand(keycloakAdminClient, REALM_LIN, user);
    return getUserGroupsCommand.execute();
  }


  private User heeUserToKeycloakUser(UserDTO userDTO) {
    Map<String, List<String>> attributes = new HashMap<>();
    List<String> dbcs = new ArrayList<>();
    attributes.put("DBC", dbcs);
    return User.create(userDTO.getKcId(), userDTO.getFirstName(), userDTO.getLastName(), userDTO.getName(),
        userDTO.getEmailAddress(), null, null, attributes, userDTO.getActive());
  }

  private User heeUserToKeycloakUser(CreateUserDTO createUserDTO) {
    Map<String, List<String>> attributes = new HashMap<>();
    List<String> dbcs = new ArrayList<>();
    attributes.put("DBC", dbcs);
    return User.create(null, createUserDTO.getFirstName(), createUserDTO.getLastName(), createUserDTO.getName(),
        createUserDTO.getEmailAddress(), createUserDTO.getPassword(), createUserDTO.getTempPassword(), attributes, createUserDTO.isActive());
  }

  @EventListener
  public void deleteKeycloakUserEventListener(DeleteKeycloakUserRequestedEvent event) {
    LOG.info("Received DeleteKeycloakUserEvent for user [{}]", event.getKcUser().getUsername());
    keycloakAdminClient.removeUser(REALM_LIN, event.getKcUser());
    if (event.isPublishDeleteProfileUserEvent()) {
      applicationEventPublisher.publishEvent(new DeleteProfileUserRequestEvent(event.getKcUser().getUsername()));
    }
  }
}
