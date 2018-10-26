package uk.nhs.hee.tis.usermanagement.config;

import com.transform.hee.tis.keycloak.KeycloakAdminClient;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakAdminClientConfig {

  @Value("${kc.master.realm}")
  private String masterRealm;

  @Value("${kc.realm}")
  private String realm;

  @Value("${kc.client.id}")
  private String clientId;

  @Value("${kc.server.url}")
  private String serverUrl;

  @Value("${kc.username}")
  private String userName;

  @Value("${kc.password}")
  private String password;

  @Bean
  public KeycloakAdminClient keycloakAdminClient() {
    KeycloakAdminClient client = new KeycloakAdminClient();
    client.init(serverUrl, masterRealm, clientId, userName, password);
    return client;
  }
}
