package uk.nhs.hee.tis.usermanagement.service;

import com.google.common.base.Preconditions;
import com.transform.hee.tis.keycloak.KeycloakAdminClient;
import com.transform.hee.tis.keycloak.User;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.keycloak.representations.idm.GroupRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.nhs.hee.tis.usermanagement.DTOs.AuthenticationUserDto;
import uk.nhs.hee.tis.usermanagement.DTOs.CreateUserDTO;
import uk.nhs.hee.tis.usermanagement.DTOs.UserDTO;
import uk.nhs.hee.tis.usermanagement.command.keycloak.GetUserAttributesCommand;
import uk.nhs.hee.tis.usermanagement.command.keycloak.GetUserCommand;
import uk.nhs.hee.tis.usermanagement.command.keycloak.GetUserGroupsCommand;
import uk.nhs.hee.tis.usermanagement.command.keycloak.UpdateUserCommand;
import uk.nhs.hee.tis.usermanagement.event.CreateAuthenticationUserRequestedEvent;
import uk.nhs.hee.tis.usermanagement.event.CreateProfileUserRequestedEvent;
import uk.nhs.hee.tis.usermanagement.event.DeleteAuthenticationUserRequestedEvent;
import uk.nhs.hee.tis.usermanagement.event.DeleteProfileUserRequestEvent;
import uk.nhs.hee.tis.usermanagement.exception.PasswordException;
import uk.nhs.hee.tis.usermanagement.exception.UserNotFoundException;
import uk.nhs.hee.tis.usermanagement.mapper.KeycloakUserMapper;

@Service
public class KeyCloakAdminClientService implements AuthenticationAdminService {

  static final String REALM_LIN = "lin";

  private static final Logger LOG = LoggerFactory.getLogger(KeyCloakAdminClientService.class);

  private static final String NAME = "Keycloak";

  @Autowired
  private KeycloakAdminClient keycloakAdminClient;

  @Autowired
  private ApplicationEventPublisher applicationEventPublisher;

  @Autowired
  private KeycloakUserMapper mapper;

  @Override
  public String getServiceName() {
    return NAME;
  }

  @Override
  public Optional<AuthenticationUserDto> getUser(String username) {
    Optional<User> kcUser = getKcUser(username);
    return kcUser.map(user -> mapper.toAuthenticationUser(user));
  }

  /**
   * Get the keycloak user for the given username,
   *
   * @param username The username to get the user for.
   * @return Optional user, empty if not found.
   */
  private Optional<User> getKcUser(String username) {
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
  public void createUserEventListener(CreateAuthenticationUserRequestedEvent event) {
    // create user in KeyCloak
    LOG.info("Received CreateKeycloakUserEvent for user [{}]",
        event.getUserDTO().getEmailAddress());
    User userToCreate = heeUserToKeycloakUser(event.getUserDTO());
    User kcUser = keycloakAdminClient.createUser(REALM_LIN, userToCreate);
    AuthenticationUserDto authenticationUser = mapper.toAuthenticationUser(kcUser);
    applicationEventPublisher.publishEvent(
        new CreateProfileUserRequestedEvent(event.getUserToCreateInProfileService(),
            authenticationUser));
  }

  @Override
  public boolean updateUser(AuthenticationUserDto authenticationUser) {
    Preconditions.checkNotNull(authenticationUser, "cannot update user if user is null");
    User kcUser = mapper.toKeycloakUser(authenticationUser);
    return updateUser(kcUser);
  }

  @Override
  public boolean updateUser(UserDTO userDto) {
    Preconditions.checkNotNull(userDto, "Cannot update user in KC if the user is null");

    // Need to validate here - or check KC client behaviour
    Optional<User> existingUser = getKcUser(userDto.getName());
    if(!existingUser.isPresent()) {
      throw new UserNotFoundException(userDto.getName(), "keycloak");
    }

    User userToUpdate = heeUserToKeycloakUser(userDto);
    return updateUser(userToUpdate);
  }

  private boolean updateUser(User user) {
    Preconditions.checkNotNull(user, "cannot update user if user is null");

    UpdateUserCommand updateUserCommand = new UpdateUserCommand(keycloakAdminClient, REALM_LIN,
        user.getId(), user);
    return updateUserCommand.execute();
  }

  @Override
  public boolean updatePassword(String userId, String password, boolean tempPassword) {
    Preconditions.checkNotNull(userId);
    Preconditions.checkNotNull(password);

    boolean success = keycloakAdminClient.updateUserPassword(REALM_LIN, userId, password,
        tempPassword);
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

    GetUserAttributesCommand getUserAttributesCommand = new GetUserAttributesCommand(
        keycloakAdminClient, REALM_LIN, username);
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

    Optional<User> optionalUser = getKcUser(username);
    User user = optionalUser.orElseThrow(() -> new UserNotFoundException(username, NAME));

    GetUserGroupsCommand getUserGroupsCommand = new GetUserGroupsCommand(keycloakAdminClient,
        REALM_LIN, user);
    return getUserGroupsCommand.execute();
  }


  private User heeUserToKeycloakUser(UserDTO userDTO) {
    Map<String, List<String>> attributes = new HashMap<>();
    List<String> dbcs = new ArrayList<>();
    attributes.put("DBC", dbcs);
    return User.create(userDTO.getKcId(), userDTO.getFirstName(), userDTO.getLastName(),
        userDTO.getName(),
        userDTO.getEmailAddress(), null, null, attributes, userDTO.getActive());
  }

  private User heeUserToKeycloakUser(CreateUserDTO createUserDTO) {
    Map<String, List<String>> attributes = new HashMap<>();
    List<String> dbcs = new ArrayList<>();
    attributes.put("DBC", dbcs);
    return User.create(null, createUserDTO.getFirstName(), createUserDTO.getLastName(),
        createUserDTO.getName(),
        createUserDTO.getEmailAddress(), createUserDTO.getPassword(),
        createUserDTO.getTempPassword(), attributes, createUserDTO.isActive());
  }

  @EventListener
  public void deleteKeycloakUserEventListener(DeleteAuthenticationUserRequestedEvent event) {
    LOG.info("Received DeleteAuthenticationUserEvent for user [{}]",
        event.getAuthenticationUser().getUsername());
    User kcUser = mapper.toKeycloakUser(event.getAuthenticationUser());
    keycloakAdminClient.removeUser(REALM_LIN, kcUser);
    if (event.isPublishDeleteProfileUserEvent()) {
      applicationEventPublisher.publishEvent(
          new DeleteProfileUserRequestEvent(event.getAuthenticationUser().getUsername()));
    }
  }
}
