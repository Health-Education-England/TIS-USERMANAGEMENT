package uk.nhs.hee.tis.usermanagement.config;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.audit.AuditEventRepository;

public class AuditingAspectConfigurationTest {

  private AuditingAspectConfiguration configuration;

  @BeforeEach
  void setUp() {
    configuration = new AuditingAspectConfiguration();
  }

  @Test
  void shouldCreateAuditEventRepository() {
    AuditEventRepository auditEventRepository = configuration.auditEventRepository();
    assertThat("Unexpected audit event repository.", auditEventRepository, notNullValue());
  }
}
