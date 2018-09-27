package uk.nhs.hee.tis.usermanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ProfileClientLocalConfig extends com.transformuk.hee.tis.profile.client.config.ProfileClientConfig {

  @Bean
  public RestTemplate profileRestTemplate() {
    return super.defaultProfileRestTemplate();
  }
}
