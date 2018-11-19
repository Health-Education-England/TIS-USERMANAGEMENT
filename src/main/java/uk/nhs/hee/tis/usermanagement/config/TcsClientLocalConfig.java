package uk.nhs.hee.tis.usermanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

@Configuration
@Profile("default")
public class TcsClientLocalConfig extends com.transformuk.hee.tis.tcs.client.config.TcsClientConfig {

  @Bean
  public RestTemplate tcsRestTemplate() {
    return super.defaultTcsRestTemplate();
  }
}