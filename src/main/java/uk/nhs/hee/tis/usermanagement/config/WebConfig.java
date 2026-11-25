package uk.nhs.hee.tis.usermanagement.config;

import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Value("${application.under-maintenance}")
  private boolean underMaintenance;

  @Value("${application.maintenance-requests}")
  private String[] maintenanceRequestArray;

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
    PageableHandlerMethodArgumentResolver resolver = new PageableHandlerMethodArgumentResolver();
    resolver.setOneIndexedParameters(true);
    argumentResolvers.add(resolver);
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    if (underMaintenance) {
      registry.addInterceptor(new UnderMaintenanceInterceptor())
          .addPathPatterns(maintenanceRequestArray);
    }
  }

  private static class UnderMaintenanceInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
        Object handler) throws IOException {
      response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
      response.setContentType("application/json");
      response.getWriter().write(
          "{\"message\":\"User Management is currently under maintenance. Please try again later.\"}");
      return false;
    }
  }
}
