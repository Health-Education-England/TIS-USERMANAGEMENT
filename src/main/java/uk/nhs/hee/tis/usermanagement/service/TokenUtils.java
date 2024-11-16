package uk.nhs.hee.tis.usermanagement.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.stereotype.Component;

@Component
public class TokenUtils {

  public  static String getIdToken() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication instanceof OAuth2AuthenticationToken) {
      OAuth2AuthenticationToken auth = (OAuth2AuthenticationToken) authentication;
      if (auth.getPrincipal() instanceof OidcUser) {
        OidcUser oidcUser = (OidcUser) auth.getPrincipal();
        OidcIdToken idToken = oidcUser.getIdToken();
        return idToken.getTokenValue();
      }
    }

    throw new RuntimeException("ID token not available or not authenticated via OAuth2");
  }
}
