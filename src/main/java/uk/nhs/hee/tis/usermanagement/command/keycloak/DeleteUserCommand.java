package uk.nhs.hee.tis.usermanagement.command.keycloak;

import com.google.gson.Gson;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.transform.hee.tis.keycloak.KeycloakAdminClient;
import com.transform.hee.tis.keycloak.User;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteUserCommand extends HystrixCommand<Boolean> {

  private static final String COMMAND_KEY = "KEYCLOAK_COMMAND";
  private static final Logger LOG = LoggerFactory.getLogger(DeleteUserCommand.class);
  private static final Gson GSON = new Gson();
  private static final int TWO_SECOND_TIMEOUT_IN_MILLIS = 2000;

  private String realm;
  private User userToDelete;
  private KeycloakAdminClient keycloakAdminClient;
  private Throwable throwable;

  public DeleteUserCommand(String realm, User userToDelete, KeycloakAdminClient keycloakAdminClient) {
    super(HystrixCommandGroupKey.Factory.asKey(COMMAND_KEY), TWO_SECOND_TIMEOUT_IN_MILLIS);
    this.realm = realm;
    this.userToDelete = userToDelete;
    this.keycloakAdminClient = keycloakAdminClient;
  }

  @Override
  protected Boolean getFallback() {
    LOG.warn("Could not delete user from KC, running fallback method");
    LOG.debug("Data sent in request realm: [{}] user: [{}]", this.realm, GSON.toJson(this.userToDelete));
    LOG.debug("Exception if any: [{}]", throwable != null ? ExceptionUtils.getStackTrace(throwable) : StringUtils.EMPTY);
    return false;
  }

  @Override
  protected Boolean run() throws Exception {
    try {
      keycloakAdminClient.removeUser(this.realm, this.userToDelete);
    } catch (Throwable e) {
      this.throwable = e;
    }
    return true;
  }
}
