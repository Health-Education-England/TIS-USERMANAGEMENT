package uk.nhs.hee.tis.usermanagement.config;

import com.transformuk.hee.tis.security.factory.InternalClientRequestFactory;
import com.transformuk.hee.tis.security.util.TisSecurityHelper;
import org.apache.http.client.methods.HttpUriRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
@Profile(value = {"uidev", "uat", "dev", "stage", "prod"})
public class ProfileClientNonLocalConfig extends com.transformuk.hee.tis.profile.client.config.ProfileClientConfig {

  @Bean
  public RestTemplate profileRestTemplate() {
    return new RestTemplate(new InternalClientRequestFactory());
  }
}
