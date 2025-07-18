package uk.nhs.hee.tis.usermanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.ses.SesClient;

/**
 * Configuration for AWS SES.
 */
@Configuration
public class AwsSesConfig {

  @Bean
  public SesClient sesClient() {
    return SesClient.create();
  }
}

