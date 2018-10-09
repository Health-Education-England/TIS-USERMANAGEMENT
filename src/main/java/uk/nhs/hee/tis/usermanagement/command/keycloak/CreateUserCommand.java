package uk.nhs.hee.tis.usermanagement.command.keycloak;

import com.google.gson.Gson;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.transform.hee.tis.keycloak.KeycloakAdminClient;
import com.transform.hee.tis.keycloak.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateUserCommand extends HystrixCommand<Boolean> {

  private static final String COMMAND_KEY = "KEYCLOAK_COMMAND";
  private static final Logger LOG = LoggerFactory.getLogger(CreateUserCommand.class);
  private static final Gson GSON = new Gson();

  private String realm;
  private User userToCreate;
  private KeycloakAdminClient keycloakAdminClient;

  public CreateUserCommand(KeycloakAdminClient keycloakAdminClient, String realm, User userToCreate) {
    super(HystrixCommandGroupKey.Factory.asKey(COMMAND_KEY));
    this.keycloakAdminClient = keycloakAdminClient;
    this.realm = realm;
    this.userToCreate = userToCreate;
  }


  @Override
  protected Boolean getFallback() {
    LOG.warn("An error occurred while attempting to create a user in KC, running fallback method");
    LOG.debug("Data used to make call, realm: [{}], userToCreate: [{}]", realm, GSON.toJson(userToCreate));
    return false;
  }

  @Override
  protected Boolean run() throws Exception {
    //ughh
    keycloakAdminClient.createUser(realm, userToCreate);
    return true;
  }
}
