package uk.nhs.hee.tis.usermanagement.command.keycloak;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.transform.hee.tis.keycloak.KeycloakAdminClient;
import com.transform.hee.tis.keycloak.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class GetUserCommand extends HystrixCommand<Optional<User>> {

  private static final String COMMAND_KEY = "KEYCLOAK_COMMAND";
  private static final Logger LOG = LoggerFactory.getLogger(GetUserCommand.class);

  private KeycloakAdminClient keycloakAdminClient;
  private String username;
  private String realm;

  public GetUserCommand(KeycloakAdminClient keycloakAdminClient, String username, String realm) {
    super(HystrixCommandGroupKey.Factory.asKey(COMMAND_KEY));
    this.keycloakAdminClient = keycloakAdminClient;
    this.username = username;
    this.realm = realm;
  }

  @Override
  protected Optional<User> getFallback() {
    LOG.warn("An error occurred while finding a KC user by username, returning empty optional...");
    LOG.debug("Data used for search, username: [{}], realm: [{}]", username, realm);
    return Optional.empty();
  }

  @Override
  protected Optional<User> run() throws Exception {
    User foundUser = keycloakAdminClient.findByUsername(realm, username);
    return Optional.ofNullable(foundUser);
  }
}
