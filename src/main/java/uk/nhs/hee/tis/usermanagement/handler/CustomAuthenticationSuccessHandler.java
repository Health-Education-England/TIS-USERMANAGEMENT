package uk.nhs.hee.tis.usermanagement.handler;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import uk.nhs.hee.tis.usermanagement.service.ProfileServiceClient;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
  private final ProfileServiceClient profileServiceClient;
  private final String userManagementUrl;

  public CustomAuthenticationSuccessHandler(ProfileServiceClient profileServiceClient,
      @Value("${usermanagement.service.url}") String userManagementUrl) {
    this.profileServiceClient = profileServiceClient;
    this.userManagementUrl = userManagementUrl;
  }

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
    profileServiceClient.getProfileData();
    response.sendRedirect(userManagementUrl + "/allUsers");
  }
}
