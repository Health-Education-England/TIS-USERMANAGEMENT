package uk.nhs.hee.tis.usermanagement.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.amazonaws.services.cognitoidp.model.UserType;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

  private CognitoResultMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = new CognitoResultMapperImpl();
  }

  @Test
  void shouldMapUserTypetoAuthenticationUser() {
    UserType userType = new UserType();
    userType.setEnabled(ENABLED);

    AttributeType attrPreferredName = new AttributeType().withName(ATTR_NAME_PREFERRED_USERNAME)
        .withValue(PREFERRED_USERNAME);
    AttributeType attrGivenName = new AttributeType().withName(ATTR_NAME_GIVEN_NAME)
        .withValue(GIVEN_NAME);
    AttributeType attrFamilyName = new AttributeType().withName(ATTR_NAME_FAMILY_NAME)
        .withValue(FAMILY_NAME);
    AttributeType attrEmail = new AttributeType().withName(ATTR_NAME_EMAIL).withValue(EMAIL);
    AttributeType attrSub = new AttributeType().withName(ATTR_NAME_SUB).withValue(SUB);
    AttributeType attrEmailVerified = new AttributeType().withName(ATTR_EMAIL_VERIFIED)
        .withValue(EMAIL_VERIFIED);

    userType.setAttributes(
        Lists.newArrayList(attrPreferredName, attrGivenName, attrFamilyName, attrEmail, attrSub,
            attrEmailVerified));

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
}
