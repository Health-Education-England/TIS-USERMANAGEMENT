package uk.nhs.hee.tis.usermanagement.command.keycloak;

import com.transform.hee.tis.keycloak.KeycloakAdminClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GetUserAttributesCommand extends KeycloakHystrixCommand<Map<String, List<String>>> {

  private static final Logger LOG = LoggerFactory.getLogger(CreateUserCommand.class);

  private KeycloakAdminClient keycloakAdminClient;
  private String realm;
  private String username;
  private Throwable throwable;

  public GetUserAttributesCommand(KeycloakAdminClient keycloakAdminClient, String realm, String username) {
    this.keycloakAdminClient = keycloakAdminClient;
    this.realm = realm;
    this.username = username;
  }

  @Override
  protected Map<String, List<String>> getFallback() {
    LOG.warn("An error occurred while attempting to get attributes for a user in KC, returning empty map");
    LOG.debug("Data used to make call, realm: [{}], username: [{}]", realm, username);
    return Collections.EMPTY_MAP;
  }

  @Override
  protected Map<String, List<String>> run() throws Exception {
    try {
      return keycloakAdminClient.getAttributesForUser(realm, username);
    } catch (Throwable e) {
      this.throwable = e;
      throw e;
    }
  }
}
