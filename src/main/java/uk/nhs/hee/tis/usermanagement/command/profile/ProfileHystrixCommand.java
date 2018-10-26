package uk.nhs.hee.tis.usermanagement.command.profile;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;

public abstract class ProfileHystrixCommand<T> extends HystrixCommand<T> {

  private static final String COMMAND_KEY = "PROFILE_COMMANDS";

  public ProfileHystrixCommand() {
    super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(COMMAND_KEY))
        .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
            .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE)));
  }
}
