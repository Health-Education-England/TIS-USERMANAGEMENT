package uk.nhs.hee.tis.usermanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

@Configuration
@Profile("local")
public class ReferenceClientLocalConfig extends com.transformuk.hee.tis.reference.client.config.ReferenceClientConfig {

  @Bean
  public RestTemplate referenceRestTemplate() {
    return super.defaultReferenceRestTemplate();
  }
}