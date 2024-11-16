package uk.nhs.hee.tis.usermanagement.config;

import com.transformuk.hee.tis.profile.client.config.JwtSpringSecurityConfig;
import com.transformuk.hee.tis.security.JwtAuthenticationEntryPoint;
import com.transformuk.hee.tis.security.JwtAuthenticationProvider;
import com.transformuk.hee.tis.security.RestAccessDeniedHandler;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;
import uk.nhs.hee.tis.usermanagement.handler.CustomAuthenticationSuccessHandler;
import uk.nhs.hee.tis.usermanagement.service.CustomCognitoEntryPoint;

@Configuration
@EnableWebSecurity
@EnableAutoConfiguration
@Import(JwtSpringSecurityConfig.class)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  @Autowired
  private JwtAuthenticationEntryPoint unauthorizedHandler;
  @Autowired
  private RestAccessDeniedHandler accessDeniedHandler;
  @Autowired
  private JwtAuthenticationProvider authenticationProvider;
  private final CustomAuthenticationSuccessHandler successHandler;

  public WebSecurityConfig(CustomAuthenticationSuccessHandler successHandler) {
    this.successHandler = successHandler;
  }

  @Bean
  @Override
  public AuthenticationManager authenticationManager() {
    return new ProviderManager(Arrays.asList(authenticationProvider));
  }

  @Override
  protected void configure(HttpSecurity httpSecurity) throws Exception {
    httpSecurity.csrf()
        .and()
        .authorizeRequests()
        .anyRequest().authenticated()
        .and()
        .oauth2Login()
        .successHandler(successHandler);
  }

  /*@Bean
  public AuthenticationEntryPoint customEntryPoint() {
    return new CustomCognitoEntryPoint();
  }

  protected void configure(HttpSecurity http) throws Exception {
    http
        .csrf().disable()
        .authorizeRequests()
        .anyRequest().authenticated()
        .and()
        .exceptionHandling()
        .authenticationEntryPoint(customEntryPoint());  // Use custom entry point
  }*/
  //npn have slashes, allowing GET requests with slashes through https://stackoverflow.com/a/41593282
  @Bean
  public HttpFirewall allowUrlEncodedSlashHttpFirewall() {
    DefaultHttpFirewall firewall = new DefaultHttpFirewall();
    firewall.setAllowUrlEncodedSlash(true);
    return firewall;
  }

  @Override
  public void configure(WebSecurity web) {
    web.httpFirewall(allowUrlEncodedSlashHttpFirewall());
  }
}
