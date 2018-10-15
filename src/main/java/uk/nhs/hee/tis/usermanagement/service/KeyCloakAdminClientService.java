package uk.nhs.hee.tis.usermanagement.service;

import com.google.common.base.Preconditions;
import com.transform.hee.tis.keycloak.KeycloakAdminClient;
import com.transform.hee.tis.keycloak.User;
import org.keycloak.representations.idm.GroupRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.nhs.hee.tis.usermanagement.DTOs.UserDTO;
import uk.nhs.hee.tis.usermanagement.command.keycloak.CreateUserCommand;
import uk.nhs.hee.tis.usermanagement.command.keycloak.GetUserAttributesCommand;
import uk.nhs.hee.tis.usermanagement.command.keycloak.GetUserCommand;
import uk.nhs.hee.tis.usermanagement.command.keycloak.GetUserGroupsCommand;
import uk.nhs.hee.tis.usermanagement.command.keycloak.UpdateUserCommand;
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

  @Autowired
  private KeycloakAdminClient keycloakAdminClient;

  public Optional<User> getUser(String username) {
    Preconditions.checkNotNull(username, "Cannot get user if username is null");

    GetUserCommand getUserCommand = new GetUserCommand(keycloakAdminClient, username, REALM_LIN);
    return getUserCommand.execute();
  }

  /**
   * Create user in Keycloak
   *
   * @param UserDTO
   */
  public boolean createUser(UserDTO UserDTO) {
    // create user in KeyCloak
    User userToCreate = heeUserToKeycloakUser(UserDTO);
    CreateUserCommand createUserCommand = new CreateUserCommand(keycloakAdminClient, REALM_LIN, userToCreate);
    return createUserCommand.execute();
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

  public void updatePassword(String userId, String password, boolean tempPassword) {
    Preconditions.checkNotNull(userId);
    Preconditions.checkNotNull(password);

    boolean success = keycloakAdminClient.updateUserPassword(REALM_LIN, userId, password, tempPassword);
    if (!success) {
      throw new PasswordException("Update password with KC failed");
    }
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
    User user = optionalUser.orElseThrow(() -> new UserNotFoundException(username, "keycloak"));

    GetUserGroupsCommand getUserGroupsCommand = new GetUserGroupsCommand(keycloakAdminClient, REALM_LIN, user);
    return getUserGroupsCommand.execute();
  }


  private User heeUserToKeycloakUser(UserDTO UserDTO) {
    Map<String, List<String>> attributes = new HashMap<>();
    List<String> dbcs = new ArrayList<>();
    attributes.put("DBC", dbcs);
    return User.create(UserDTO.getKcId(), UserDTO.getFirstName(), UserDTO.getLastName(), UserDTO.getName(),
        UserDTO.getEmailAddress(), null, null, attributes, UserDTO.getActive());
  }
}
