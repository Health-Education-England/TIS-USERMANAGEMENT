package uk.nhs.hee.tis.usermanagement.config;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Cognito.
 */
@Configuration
public class CognitoAdminClientConfig {

  /**
   * Create a default Cognito IDP client.
   */
  @Bean
  public AWSCognitoIdentityProvider awsCognitoIdentityProvider() {
    return AWSCognitoIdentityProviderClientBuilder.defaultClient();
  }
}
