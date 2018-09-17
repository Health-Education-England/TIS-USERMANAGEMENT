package uk.nhs.hee.tis.usermanagement.config;

import com.transformuk.hee.tis.reference.client.impl.ReferenceServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReferenceServiceImplConfig {
  private static final double RATE_LIMIT = 10d;
  private static final double BULK_RATE_LIMIT = 10d;

  @Bean
  public ReferenceServiceImpl referenceServiceImpl() {
    return new ReferenceServiceImpl(RATE_LIMIT, BULK_RATE_LIMIT);
  }
}