package uk.nhs.hee.tis.usermanagement.config;

import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {

  @Value("${application.under-maintenance}")
  private boolean underMaintenance;

  @Value("${application.maintenance-requests}")
  private String[] maintenanceRequestArray;

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

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    if (underMaintenance) {
      registry.addInterceptor(new UnderMaintenanceInterceptor())
          .addPathPatterns(maintenanceRequestArray);
      super.addInterceptors(registry);
    }
  }

  private class UnderMaintenanceInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
        Object handler) throws IOException {
      response.sendRedirect("userNotification");
      return false;
    }
  }
}
