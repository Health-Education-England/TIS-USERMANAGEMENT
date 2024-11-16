package uk.nhs.hee.tis.usermanagement.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;
import uk.nhs.hee.tis.usermanagement.service.TokenUtils;

@Configuration
public class RestTemplateConfig {
  private final TokenUtils tokenUtils;

  public RestTemplateConfig(TokenUtils tokenUtils){
    this.tokenUtils = tokenUtils;

  }

  @Bean
  public RestTemplate restTemplate() {
    RestTemplate restTemplate = new RestTemplate();

    List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
    interceptors.add(new ClientHttpRequestInterceptor() {
      @Override
      public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        String idToken = tokenUtils.getIdToken();

        if (idToken != null) {
          request.getHeaders().add(HttpHeaders.AUTHORIZATION, "Bearer " + idToken);
        }
        return execution.execute(request, body);
      }
    });
    restTemplate.setInterceptors(interceptors);
    return restTemplate;
  }
}
