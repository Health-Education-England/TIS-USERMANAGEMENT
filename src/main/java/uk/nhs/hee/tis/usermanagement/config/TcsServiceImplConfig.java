package uk.nhs.hee.tis.usermanagement.config;

import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TcsServiceImplConfig {
  private static final double RATE_LIMIT = 10d;
  private static final double BULK_RATE_LIMIT = 10d;

  @Bean
  public TcsServiceImpl tcsServiceImpl() {
    return new TcsServiceImpl(RATE_LIMIT, BULK_RATE_LIMIT);
  }
}