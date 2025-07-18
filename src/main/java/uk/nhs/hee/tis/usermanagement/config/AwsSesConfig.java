package uk.nhs.hee.tis.usermanagement.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.ses.SesClient;

@Configuration
@ConditionalOnProperty(name = "application.authentication-provider", havingValue = "cognito")
public class AwsSesConfig {

  @Bean
  public SesClient sesClient() {
    return SesClient.create();
  }
}

