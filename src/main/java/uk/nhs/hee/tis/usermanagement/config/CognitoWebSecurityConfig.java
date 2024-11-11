package uk.nhs.hee.tis.usermanagement.config;

import com.transformuk.hee.tis.profile.client.config.JwtSpringSecurityConfig;
import com.transformuk.hee.tis.security.JwtAuthenticationProvider;
import com.transformuk.hee.tis.security.JwtAuthenticationSuccessHandler;
import com.transformuk.hee.tis.security.filter.JwtAuthenticationTokenFilter;
import java.util.Arrays;
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
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;

@Configuration
@ConditionalOnProperty(name = "application.authentication-provider", havingValue = "cognito")
@EnableWebSecurity
@EnableAutoConfiguration
@Import(JwtSpringSecurityConfig.class)
public class CognitoWebSecurityConfig extends WebSecurityConfigurerAdapter {

  @Autowired
  private JwtAuthenticationProvider authenticationProvider;

  @Bean
  @Override
  public AuthenticationManager authenticationManager() {
    return new ProviderManager(Arrays.asList(authenticationProvider));
  }

  @Bean
  public JwtAuthenticationTokenFilter authenticationTokenFilterBean() {
    JwtAuthenticationTokenFilter authenticationTokenFilter = new JwtAuthenticationTokenFilter(
        "/**");
    authenticationTokenFilter.setAuthenticationManager(authenticationManager());
    authenticationTokenFilter
        .setAuthenticationSuccessHandler(new JwtAuthenticationSuccessHandler());
    return authenticationTokenFilter;
  }

  @Override
  protected void configure(HttpSecurity httpSecurity) throws Exception {
    httpSecurity
        // we don't need CSRF because our token is invulnerable
        .csrf().disable()
        .authorizeRequests().antMatchers("/**").permitAll() //TODO: Check use of `permitAll()`
        .and()
        .oauth2Login();
//    httpSecurity
//        // we don't need CSRF because our token is invulnerable
//        .csrf().disable()
//        .authorizeRequests().antMatchers("/","/usermanagement/error", "/error").permitAll()
//        .and()
//        .authorizeRequests().anyRequest().authenticated()
//        .and()
//        .oauth2Login();
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
