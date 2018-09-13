package uk.nhs.hee.tis.usermanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ReferenceClientLocalConfig extends com.transformuk.hee.tis.reference.client.config.ReferenceClientConfig{

  @Bean
  public RestTemplate referenceRestTemplate() { return super.defaultReferenceRestTemplate(); }
}