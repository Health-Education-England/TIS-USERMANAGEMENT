package uk.nhs.hee.tis.usermanagement.config;

import com.transformuk.hee.tis.security.factory.InternalClientRequestFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

@Configuration
//@Profile(value = {"uidev", "uat", "dev", "stage", "prod"})
public class ReferenceClientNonLocalConfig extends com.transformuk.hee.tis.reference.client.config.ReferenceClientConfig {

  @Bean
  public RestTemplate referenceRestTemplate() {
    return new RestTemplate(new InternalClientRequestFactory());
  }
}