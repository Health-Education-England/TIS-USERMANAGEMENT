package uk.nhs.hee.tis.usermanagement.config;

import com.transformuk.hee.tis.profile.client.config.JwtSpringSecurityConfig;
import com.transformuk.hee.tis.security.JwtAuthenticationEntryPoint;
import com.transformuk.hee.tis.security.JwtAuthenticationProvider;
import com.transformuk.hee.tis.security.JwtAuthenticationSuccessHandler;
import com.transformuk.hee.tis.security.RestAccessDeniedHandler;
import com.transformuk.hee.tis.security.filter.JwtAuthenticationTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;

import java.util.Arrays;

@Configuration
@ConditionalOnProperty(name = "application.authentication-provider", havingValue = "keycloak")
@EnableWebSecurity
@EnableAutoConfiguration
@Import(JwtSpringSecurityConfig.class)
public class KeycloakWebSecurityConfig extends WebSecurityConfigurerAdapter {

  @Autowired
  private JwtAuthenticationEntryPoint unauthorizedHandler;
  @Autowired
  private RestAccessDeniedHandler accessDeniedHandler;
  @Autowired
  private JwtAuthenticationProvider authenticationProvider;

  @Bean
  @Override
  public AuthenticationManager authenticationManager() {
    return new ProviderManager(Arrays.asList(authenticationProvider));
  }

  @Bean
  public JwtAuthenticationTokenFilter authenticationTokenFilterBean() {
    JwtAuthenticationTokenFilter authenticationTokenFilter = new JwtAuthenticationTokenFilter("/**");
    authenticationTokenFilter.setAuthenticationManager(authenticationManager());
    authenticationTokenFilter.setAuthenticationSuccessHandler(new JwtAuthenticationSuccessHandler());
    return authenticationTokenFilter;
  }

  @Override
  protected void configure(HttpSecurity httpSecurity) throws Exception {
    httpSecurity
        // to allow browser to render contents in frames
        .headers().frameOptions().sameOrigin()
        .and()
        // we don't need CSRF because our token is invulnerable
        .csrf().disable()
//        .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).accessDeniedHandler(accessDeniedHandler)
        .authorizeRequests().anyRequest().authenticated()
        .and()
        .oauth2Login();
  }

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
