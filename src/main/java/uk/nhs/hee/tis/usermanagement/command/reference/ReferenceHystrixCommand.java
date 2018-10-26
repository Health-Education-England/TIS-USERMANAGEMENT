package uk.nhs.hee.tis.usermanagement.command.reference;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;

public abstract class ReferenceHystrixCommand<T> extends HystrixCommand<T> {

  private static final String COMMAND_KEY = "REFERENCE_COMMANDS";

  public ReferenceHystrixCommand() {
    super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(COMMAND_KEY))
        .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
            .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE)));
  }
}
