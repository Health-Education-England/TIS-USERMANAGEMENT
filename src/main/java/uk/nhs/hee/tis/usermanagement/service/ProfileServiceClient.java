package uk.nhs.hee.tis.usermanagement.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ProfileServiceClient {

  private final RestTemplate restTemplate;
  private final String profileUrl;

  public ProfileServiceClient(RestTemplate restTemplate, @Value("${profile.service.url}")String profileUrl) {
    this.restTemplate = restTemplate;
    this.profileUrl = profileUrl;
  }

  public String getProfileData() {
    String url = profileUrl+"/api/userinfo";
    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY, String.class);

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    List<GrantedAuthority> updatedAuthorities = new ArrayList<>(auth.getAuthorities());
    // This need to be handled dynamically in pre token generation lambda// may be a separate ticket
    updatedAuthorities.add(new SimpleGrantedAuthority("heeuser:view"));
    updatedAuthorities.add(new SimpleGrantedAuthority("heeuser:add:modify"));
    Authentication newAuth = new UsernamePasswordAuthenticationToken(auth.getPrincipal(), auth.getCredentials(), updatedAuthorities);
    SecurityContextHolder.getContext().setAuthentication(newAuth);
    return response.getBody();
  }
}
