package uk.nhs.hee.tis.usermanagement.service;

import com.transform.hee.tis.keycloak.KeycloakAdminClient;
import com.transform.hee.tis.keycloak.User;
import org.keycloak.representations.idm.GroupRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.nhs.hee.tis.usermanagement.DTOs.UserDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class KeyCloakAdminClientService {
  static final String REALM_LIN = "lin";

  @Autowired
  public KeycloakAdminClient keycloakAdminClient;

  /**
   * Create user in Keycloak
   *
   * @param UserDTO
   */
  public void createUser(UserDTO UserDTO) {
    // create user in KeyCloak
    User userToCreate = heeUserToKeycloakUser(UserDTO);
    keycloakAdminClient.createUser(REALM_LIN, userToCreate);
  }

  /**
   * Update user in Keycloak
   *
   * @param UserDTO
   */
  public void updateUser(UserDTO UserDTO) {

    // Need to validate here - or check KC client behaviour

    User existingUser = getUser(UserDTO.getName());
    User userToUpdate = heeUserToKeycloakUser(UserDTO);
    keycloakAdminClient.updateUser(REALM_LIN, existingUser.getId(), userToUpdate);
  }

  public Map<String, List<String>> getUserAttributes(String username) {
    return keycloakAdminClient.getAttributesForUser(REALM_LIN, username);
  }

  public List<GroupRepresentation> getUserGroups(String username) {
    User user = getUser(username);
    List<GroupRepresentation> groupList = keycloakAdminClient.listGroups(REALM_LIN, user);
    return groupList;
  }

  public User getUser(String username) {
    User user = keycloakAdminClient.findByUsername(REALM_LIN, username);
    return user;
  }

  private User heeUserToKeycloakUser(UserDTO UserDTO) {
    Map<String, List<String>> attributes = new HashMap<>();
    List<String> dbcs = new ArrayList<>();
    attributes.put("DBC", dbcs);
    return User.create(UserDTO.getFirstName(), UserDTO.getLastName(), UserDTO.getName(),
        UserDTO.getEmailAddress(), UserDTO.getPassword(), UserDTO.getTemporaryPassword(), attributes, UserDTO.getActive());
  }
}
