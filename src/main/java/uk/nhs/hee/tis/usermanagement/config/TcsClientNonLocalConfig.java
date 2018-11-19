package uk.nhs.hee.tis.usermanagement.config;

import com.transformuk.hee.tis.security.factory.InternalClientRequestFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

@Configuration
@Profile(value = {"uidev", "uat", "dev", "stage", "prod"})
public class TcsClientNonLocalConfig extends com.transformuk.hee.tis.tcs.client.config.TcsClientConfig {

  @Bean
  public RestTemplate tcsRestTemplate() {
    return new RestTemplate(new InternalClientRequestFactory());
  }
}