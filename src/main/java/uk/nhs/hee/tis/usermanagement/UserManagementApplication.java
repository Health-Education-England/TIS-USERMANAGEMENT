package uk.nhs.hee.tis.usermanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableCaching
@Configuration
@SpringBootApplication
@EnableWebMvc
@EnableSpringDataWebSupport
//@EnableEurekaClient
@PropertySource(
    {
        "classpath:/application.properties",
        "classpath:/config/profileclientapplication.properties",
        "classpath:/config/referenceclientapplication.properties",
        "classpath:/config/tcsclientapplication.properties"
    }
)
public class UserManagementApplication {

  public static void main(String[] args) {
    SpringApplication.run(UserManagementApplication.class, args);
  }
}
