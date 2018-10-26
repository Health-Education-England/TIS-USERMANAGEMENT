package uk.nhs.hee.tis.usermanagement.command.keycloak;

import com.google.gson.Gson;
import com.transform.hee.tis.keycloak.KeycloakAdminClient;
import com.transform.hee.tis.keycloak.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateUserCommand extends KeycloakHystrixCommand<Boolean> {

  private static final Logger LOG = LoggerFactory.getLogger(CreateUserCommand.class);
  private static final Gson GSON = new Gson();

  private KeycloakAdminClient keycloakAdminClient;
  private String realm;
  private String userId;
  private User userToUpdate;
  private Throwable throwable;

  public UpdateUserCommand(KeycloakAdminClient keycloakAdminClient, String realm, String userId, User userToUpdate) {
    this.keycloakAdminClient = keycloakAdminClient;
    this.realm = realm;
    this.userId = userId;
    this.userToUpdate = userToUpdate;
  }

  @Override
  protected Boolean getFallback() {
    LOG.warn("An error occurred while updating a KC user, running fallback method");
    LOG.debug("Data used for update, realm: [{}], userId: [{}], userToUpdate [{}]", realm, userId, GSON.toJson(userToUpdate));
    return false;
  }

  @Override
  protected Boolean run() throws Exception {
    try {
      keycloakAdminClient.updateUser(realm, userId, userToUpdate);
      return true;
    } catch (Throwable e) {
      this.throwable = e;
      throw e;
    }
  }
}
