package uk.nhs.hee.tis.usermanagement.config;

import com.transformuk.hee.tis.profile.client.service.impl.ProfileServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProfileServiceImplConfig {

  private static final double RATE_LIMIT = 10d;
  private static final double BULK_RATE_LIMIT = 10d;

  @Bean
  public ProfileServiceImpl profileServiceImp(){
    return new ProfileServiceImpl(RATE_LIMIT,BULK_RATE_LIMIT);
  };
}
