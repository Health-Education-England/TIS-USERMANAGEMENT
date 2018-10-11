package uk.nhs.hee.tis.usermanagement.command.keycloak;

import com.google.gson.Gson;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.transform.hee.tis.keycloak.KeycloakAdminClient;
import com.transform.hee.tis.keycloak.User;
import org.keycloak.representations.idm.GroupRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public class GetUserGroupsCommand extends HystrixCommand<List<GroupRepresentation>> {

  private static final String COMMAND_KEY = "KEYCLOAK_COMMAND";
  private static final Logger LOG = LoggerFactory.getLogger(CreateUserCommand.class);
  private static final Gson GSON = new Gson();

  private KeycloakAdminClient keycloakAdminClient;
  private String realm;
  private User user;

  public GetUserGroupsCommand(KeycloakAdminClient keycloakAdminClient, String realm, User user) {
    super(HystrixCommandGroupKey.Factory.asKey(COMMAND_KEY));
    this.keycloakAdminClient = keycloakAdminClient;
    this.realm = realm;
    this.user = user;
  }

  @Override
  protected List<GroupRepresentation> getFallback() {
    LOG.warn("An error occurred while attempting to get groups for a user in KC, returning empty list");
    LOG.debug("Data used to make call, realm: [{}], user: [{}]", realm, GSON.toJson(user));
    return Collections.EMPTY_LIST;
  }

  @Override
  protected List<GroupRepresentation> run() throws Exception {
    try {
      return keycloakAdminClient.listGroups(realm, user);
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }
}
