package uk.nhs.hee.tis.usermanagement.mapper;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.transform.hee.tis.keycloak.User;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import uk.nhs.hee.tis.usermanagement.DTOs.AuthenticationUserDto;

public class KeycloakUserMapperTest {

  private static final String ID = "123-abc-456-def";
  private static final String GIVEN_NAME = "Anthony";
  private static final String FAMILY_NAME = "Gilliam";
  private static final String USERNAME = "anthony.gilliam";
  private static final String EMAIL = "anthony.gilliam@dummy.com";
  private static final String PASSWORD = "T0ny_123";
  private static final boolean IS_TEMP_PASSWORD = false;
  private static final Map<String, List<String>> ATTRIBUTES = Collections.singletonMap(
      "favourite_animals",
      Arrays.asList("Cat", "Dog", "Atretochoana"));
  private static final boolean IS_ENABLED = true;


  private KeycloakUserMapper mapper;

  @Before
  public void setUp() {
    mapper = new KeycloakUserMapperImpl();
  }

  @Test
  public void shouldMapKeycloakUserToAuthenticationUser() {
    User kcUser = User.create(ID, GIVEN_NAME, FAMILY_NAME, USERNAME, EMAIL, PASSWORD,
        IS_TEMP_PASSWORD, ATTRIBUTES, IS_ENABLED);

    AuthenticationUserDto authenticationUser = mapper.toAuthenticationUser(kcUser);

    assertThat("Unexpected id.", authenticationUser.getId(), is(ID));
    assertThat("Unexpected given name.", authenticationUser.getGivenName(), is(GIVEN_NAME));
    assertThat("Unexpected family name.", authenticationUser.getFamilyName(), is(FAMILY_NAME));
    assertThat("Unexpected username.", authenticationUser.getUsername(), is(USERNAME));
    assertThat("Unexpected email.", authenticationUser.getEmail(), is(EMAIL));
    assertThat("Unexpected password.", authenticationUser.getPassword(), is(PASSWORD));
    assertThat("Unexpected password permanence flag.", authenticationUser.isTemporaryPassword(),
        is(IS_TEMP_PASSWORD));
    assertThat("Unexpected attributes.", authenticationUser.getAttributes(), is(ATTRIBUTES));
    assertThat("Unexpected enabled flag.", authenticationUser.isEnabled(), is(IS_ENABLED));
  }

  @Test
  public void shouldMapAuthenticationUserToKeycloakUser() {
    AuthenticationUserDto authenticationUser = new AuthenticationUserDto();
    authenticationUser.setId(ID);
    authenticationUser.setGivenName(GIVEN_NAME);
    authenticationUser.setFamilyName(FAMILY_NAME);
    authenticationUser.setUsername(USERNAME);
    authenticationUser.setEmail(EMAIL);
    authenticationUser.setPassword(PASSWORD);
    authenticationUser.setTemporaryPassword(IS_TEMP_PASSWORD);
    authenticationUser.setAttributes(ATTRIBUTES);
    authenticationUser.setEnabled(IS_ENABLED);

    User kcUser = mapper.toKeycloakUser(authenticationUser);

    assertThat("Unexpected id.", kcUser.getId(), is(ID));
    assertThat("Unexpected first name.", kcUser.getFirstname(), is(GIVEN_NAME));
    assertThat("Unexpected surname.", kcUser.getSurname(), is(FAMILY_NAME));
    assertThat("Unexpected username.", kcUser.getUsername(), is(USERNAME));
    assertThat("Unexpected email.", kcUser.getEmail(), is(EMAIL));
    assertThat("Unexpected password.", kcUser.getPassword(), is(PASSWORD));
    assertThat("Unexpected password permanence flag.", kcUser.getTempPassword(),
        is(IS_TEMP_PASSWORD));
    assertThat("Unexpected attributes.", kcUser.getAttributes(), is(ATTRIBUTES));
    assertThat("Unexpected enabled flag.", kcUser.getEnabled(), is(IS_ENABLED));
  }
}
