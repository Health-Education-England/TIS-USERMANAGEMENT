package uk.nhs.hee.tis.usermanagement.config;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
  public AWSCognitoIdentityProvider awsCognitoIdentityProvider() {
    return AWSCognitoIdentityProviderClientBuilder.defaultClient();
  }
}
