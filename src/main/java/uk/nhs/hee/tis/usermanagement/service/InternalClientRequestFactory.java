package uk.nhs.hee.tis.usermanagement.service;

import org.apache.http.client.methods.HttpUriRequest;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;

@Component
public class InternalClientRequestFactory extends HttpComponentsClientHttpRequestFactory {
  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";

  public InternalClientRequestFactory() {
    super();
  }

  @Override
  protected void postProcessHttpRequest(HttpUriRequest request) {

   String token = TisSecurityHelper.getTokenFromContext();
    if (token != null) {
      request.setHeader(AUTHORIZATION_HEADER, BEARER_PREFIX + token);
    }
  }
}