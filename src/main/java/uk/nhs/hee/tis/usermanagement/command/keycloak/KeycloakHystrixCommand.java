package uk.nhs.hee.tis.usermanagement.command.keycloak;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;

public abstract class KeycloakHystrixCommand<T> extends HystrixCommand<T> {

  private static final String COMMAND_KEY = "KEYCLOAK_COMMAND";
  private static final int THIRTY_SECONDS_IN_MILLIS = 30000;

  public KeycloakHystrixCommand() {
    super(
        Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(COMMAND_KEY))
            .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE)
                .withExecutionTimeoutInMilliseconds(THIRTY_SECONDS_IN_MILLIS))
    );
  }
}
