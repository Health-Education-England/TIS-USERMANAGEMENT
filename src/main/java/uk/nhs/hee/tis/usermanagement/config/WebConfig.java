package uk.nhs.hee.tis.usermanagement.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {


  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addViewController("/").setViewName("forward:/allUsers");
    registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
    super.addViewControllers(registry);
  }

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
    PageableHandlerMethodArgumentResolver resolver = new PageableHandlerMethodArgumentResolver();
    resolver.setOneIndexedParameters(true);
    argumentResolvers.add(resolver);
    super.addArgumentResolvers(argumentResolvers);
  }
}
