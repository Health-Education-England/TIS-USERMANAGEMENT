package uk.nhs.hee.tis.usermanagement.command.keycloak;

import com.google.gson.Gson;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.transform.hee.tis.keycloak.KeycloakAdminClient;
import com.transform.hee.tis.keycloak.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Optional;

public class CreateUserCommand extends KeycloakHystrixCommand<Optional<User>> {

  private static final Logger LOG = LoggerFactory.getLogger(CreateUserCommand.class);
  private static final Gson GSON = new Gson();

  private String realm;
  private User userToCreate;
  private KeycloakAdminClient keycloakAdminClient;
  private Throwable throwable;

  public CreateUserCommand(KeycloakAdminClient keycloakAdminClient, String realm, User userToCreate) {
    this.keycloakAdminClient = keycloakAdminClient;
    this.realm = realm;
    this.userToCreate = userToCreate;
  }


  @Override
  protected Optional<User> getFallback() {
    LOG.warn("An error occurred while attempting to create a user in KC, running fallback method");
    LOG.debug("Data used to make call, realm: [{}], userToCreate: [{}]", realm, GSON.toJson(userToCreate));
    return Optional.empty();
  }

  @Override
  protected Optional<User> run() throws Exception {
    try {
      User user = keycloakAdminClient.createUser(realm, userToCreate);
      return Optional.ofNullable(user);
    } catch (Throwable e) {
      this.throwable = e;
      throw e;
    }
  }
}
