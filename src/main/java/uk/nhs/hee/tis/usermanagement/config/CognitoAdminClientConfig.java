package uk.nhs.hee.tis.usermanagement.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

/**
 * Configuration for Cognito.
 */
@Configuration
@ConditionalOnProperty(name = "application.authentication-provider", havingValue = "cognito")
public class CognitoAdminClientConfig {

  /**
   * Create a default Cognito IDP client.
   */
  @Bean
  public CognitoIdentityProviderClient awsCognitoIdentityProvider() {
    return CognitoIdentityProviderClient.create();
  }
}
