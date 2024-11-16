package uk.nhs.hee.tis.usermanagement.service;

import com.transformuk.hee.tis.iam.Permission;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.transformuk.hee.tis.security.model.UserProfile;

@Service
public class ProfileServiceClient {

  private final RestTemplate restTemplate;
  private final String profileUrl;

  public ProfileServiceClient(RestTemplate restTemplate, @Value("${profile.service.url}")String profileUrl) {
    this.restTemplate = restTemplate;
    this.profileUrl = profileUrl;
  }

  public String getProfileData() {
    String url = profileUrl + "/api/userinfo";

    ResponseEntity<UserProfile> response = restTemplate.exchange(url, HttpMethod.GET,
        HttpEntity.EMPTY, UserProfile.class);
    Set<Permission> permissions = response.getBody().getPermissionPolicies();

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    List<GrantedAuthority> authorities = new ArrayList<>(auth.getAuthorities());

    permissions.stream().forEach(
        permission -> authorities.add(new SimpleGrantedAuthority(permission.getName())));
    Authentication newAuth = new UsernamePasswordAuthenticationToken(auth.getPrincipal(),
        auth.getCredentials(), authorities);

    SecurityContextHolder.getContext().setAuthentication(newAuth);
    OidcUser user = (OidcUser)newAuth.getPrincipal();
    System.out.println("ID TOKEN  "+user.getIdToken().getTokenValue());
    return response.getBody().toString();
  }
}
