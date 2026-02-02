package uk.nhs.hee.tis.usermanagement.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserType;
import uk.nhs.hee.tis.usermanagement.DTOs.AuthenticationUserDto;

class CognitoResultMapperTest {

  private static final String ATTR_NAME_PREFERRED_USERNAME = "preferred_username";
  private static final String ATTR_NAME_GIVEN_NAME = "given_name";
  private static final String ATTR_NAME_FAMILY_NAME = "family_name";
  private static final String ATTR_NAME_EMAIL = "email";
  private static final String ATTR_NAME_SUB = "sub";
  private static final String ATTR_EMAIL_VERIFIED = "email_verified";

  private static final boolean ENABLED = true;
  private static final String EMAIL_VERIFIED = "true";
  private static final String SUB = "5e5axxxx-e1xx-4axx-xxxx-96a7f86xxxxx";
  private static final String PREFERRED_USERNAME = "anthony.gilliam@dummy.com";
  private static final String GIVEN_NAME = "Anthony";
  private static final String FAMILY_NAME = "Gilliam";
  private static final String EMAIL = "anthony.gilliam@dummy.com";
  private static final String PREFERRED_MFA = "SOFTWARE_TOKEN_MFA";
  private static final List<String> MFA_SETTINGS = Arrays.asList("EMAIL_OTP", "SOFTWARE_TOKEN_MFA");

  private CognitoResultMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = new CognitoResultMapperImpl();
  }

  @Test
  void shouldMapUserTypetoAuthenticationUser() {
    AttributeType attrPreferredName = AttributeType.builder().
        name(ATTR_NAME_PREFERRED_USERNAME)
        .value(PREFERRED_USERNAME)
        .build();
    AttributeType attrGivenName = AttributeType.builder()
        .name(ATTR_NAME_GIVEN_NAME)
        .value(GIVEN_NAME)
        .build();
    AttributeType attrFamilyName = AttributeType.builder()
        .name(ATTR_NAME_FAMILY_NAME)
        .value(FAMILY_NAME)
        .build();
    AttributeType attrEmail = AttributeType.builder()
        .name(ATTR_NAME_EMAIL)
        .value(EMAIL)
        .build();
    AttributeType attrSub = AttributeType.builder()
        .name(ATTR_NAME_SUB)
        .value(SUB)
        .build();
    AttributeType attrEmailVerified = AttributeType.builder()
        .name(ATTR_EMAIL_VERIFIED)
        .value(EMAIL_VERIFIED)
        .build();

    UserType userType = UserType.builder()
        .enabled(ENABLED)
        .attributes(List.of(attrPreferredName, attrGivenName, attrFamilyName, attrEmail, attrSub,
            attrEmailVerified))
        .build();

    AuthenticationUserDto userDto = mapper.toAuthenticationUser(userType);

    assertEquals(SUB, userDto.getId());
    assertEquals(EMAIL, userDto.getUsername());
    assertEquals(GIVEN_NAME, userDto.getGivenName());
    assertEquals(FAMILY_NAME, userDto.getFamilyName());
    assertEquals(EMAIL, userDto.getEmail());
    assertEquals(ENABLED, userDto.isEnabled());
    assertEquals(6, userDto.getAttributes().size());
    assertEquals(SUB, userDto.getAttributes().get(ATTR_NAME_SUB).get(0));
    assertEquals(EMAIL_VERIFIED, userDto.getAttributes().get(ATTR_EMAIL_VERIFIED).get(0));
    assertEquals(PREFERRED_USERNAME,
        userDto.getAttributes().get(ATTR_NAME_PREFERRED_USERNAME).get(0));
    assertEquals(GIVEN_NAME, userDto.getAttributes().get(ATTR_NAME_GIVEN_NAME).get(0));
    assertEquals(FAMILY_NAME, userDto.getAttributes().get(ATTR_NAME_FAMILY_NAME).get(0));
    assertEquals(EMAIL, userDto.getAttributes().get(ATTR_NAME_EMAIL).get(0));
  }

  @Test
  void shouldMapAdminGetUserResponseToAuthenticationUserWithMfaSettings() {
    AdminGetUserResponse adminGetUserResponse = AdminGetUserResponse.builder()
        .username(EMAIL)
        .userAttributes(
            AttributeType.builder().name(ATTR_NAME_SUB).value(SUB).build(),
            AttributeType.builder().name(ATTR_NAME_GIVEN_NAME).value(GIVEN_NAME).build(),
            AttributeType.builder().name(ATTR_NAME_FAMILY_NAME).value(FAMILY_NAME).build(),
            AttributeType.builder().name(ATTR_NAME_EMAIL).value(EMAIL).build(),
            AttributeType.builder().name(ATTR_EMAIL_VERIFIED).value(EMAIL_VERIFIED).build(),
            AttributeType.builder().name(ATTR_NAME_PREFERRED_USERNAME)
                .value(PREFERRED_USERNAME).build()
        )
        .userMFASettingList(MFA_SETTINGS)
        .preferredMfaSetting(PREFERRED_MFA)
        .enabled(ENABLED)
        .build();

    AuthenticationUserDto userDto = mapper.toAuthenticationUser(adminGetUserResponse);

    assertEquals(SUB, userDto.getId());
    assertEquals(EMAIL, userDto.getUsername());
    assertEquals(GIVEN_NAME, userDto.getGivenName());
    assertEquals(FAMILY_NAME, userDto.getFamilyName());
    assertEquals(EMAIL, userDto.getEmail());
    assertEquals(ENABLED, userDto.isEnabled());
    assertEquals(6, userDto.getAttributes().size());
    assertEquals(SUB, userDto.getAttributes().get(ATTR_NAME_SUB).get(0));
    assertEquals(EMAIL_VERIFIED, userDto.getAttributes().get(ATTR_EMAIL_VERIFIED).get(0));
    assertEquals(PREFERRED_USERNAME,
        userDto.getAttributes().get(ATTR_NAME_PREFERRED_USERNAME).get(0));
    assertEquals(GIVEN_NAME, userDto.getAttributes().get(ATTR_NAME_GIVEN_NAME).get(0));
    assertEquals(FAMILY_NAME, userDto.getAttributes().get(ATTR_NAME_FAMILY_NAME).get(0));
    assertEquals(EMAIL, userDto.getAttributes().get(ATTR_NAME_EMAIL).get(0));
    assertEquals(PREFERRED_MFA, userDto.getPreferredMfaSetting());
    assertEquals(MFA_SETTINGS, userDto.getUserMfaSettingList());
  }

  @Test
  void shouldMapAdminGetUserResponseToAuthenticationUserWithoutMfaSettings() {
    AdminGetUserResponse adminGetUserResponse = AdminGetUserResponse.builder()
        .username(EMAIL)
        .userAttributes(
            AttributeType.builder().name(ATTR_NAME_SUB).value(SUB).build(),
            AttributeType.builder().name(ATTR_NAME_GIVEN_NAME).value(GIVEN_NAME).build(),
            AttributeType.builder().name(ATTR_NAME_FAMILY_NAME).value(FAMILY_NAME).build(),
            AttributeType.builder().name(ATTR_NAME_EMAIL).value(EMAIL).build(),
            AttributeType.builder().name(ATTR_EMAIL_VERIFIED).value(EMAIL_VERIFIED).build(),
            AttributeType.builder().name(ATTR_NAME_PREFERRED_USERNAME)
                .value(PREFERRED_USERNAME).build()
        )
        .enabled(ENABLED)
        .build();

    AuthenticationUserDto userDto = mapper.toAuthenticationUser(adminGetUserResponse);

    assertEquals(SUB, userDto.getId());
    assertEquals(EMAIL, userDto.getUsername());
    assertEquals(GIVEN_NAME, userDto.getGivenName());
    assertEquals(FAMILY_NAME, userDto.getFamilyName());
    assertEquals(EMAIL, userDto.getEmail());
    assertEquals(ENABLED, userDto.isEnabled());
    assertEquals(6, userDto.getAttributes().size());
    assertEquals(SUB, userDto.getAttributes().get(ATTR_NAME_SUB).get(0));
    assertEquals(EMAIL_VERIFIED, userDto.getAttributes().get(ATTR_EMAIL_VERIFIED).get(0));
    assertEquals(PREFERRED_USERNAME,
        userDto.getAttributes().get(ATTR_NAME_PREFERRED_USERNAME).get(0));
    assertEquals(GIVEN_NAME, userDto.getAttributes().get(ATTR_NAME_GIVEN_NAME).get(0));
    assertEquals(FAMILY_NAME, userDto.getAttributes().get(ATTR_NAME_FAMILY_NAME).get(0));
    assertEquals(EMAIL, userDto.getAttributes().get(ATTR_NAME_EMAIL).get(0));
    assertNull(userDto.getPreferredMfaSetting());
    assertNull(userDto.getUserMfaSettingList());
  }
}
