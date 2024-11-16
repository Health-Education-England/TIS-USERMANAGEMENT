package uk.nhs.hee.tis.usermanagement.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 * Helper class to provide utility methods to retrieve data from {@link SecurityContextHolder}
 */
public class TisSecurityHelper {

  public static String getTokenFromContext() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null) {
      throw new RuntimeException("Authentication is null in the current security context.");
    }

    if (authentication.getPrincipal() instanceof OidcUser) {
      OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
      return oidcUser.getIdToken().getTokenValue();
    }
    throw new RuntimeException("Cannot find JwtAuthToken in the current security context.");
  }
}
