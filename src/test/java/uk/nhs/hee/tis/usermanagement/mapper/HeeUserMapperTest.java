package uk.nhs.hee.tis.usermanagement.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.transformuk.hee.tis.profile.dto.RoleDTO;
import com.transformuk.hee.tis.profile.service.dto.HeeUserDTO;
import com.transformuk.hee.tis.profile.service.dto.UserProgrammeDTO;
import com.transformuk.hee.tis.profile.service.dto.UserTrustDTO;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.nhs.hee.tis.usermanagement.DTOs.AuthenticationUserDto;
import uk.nhs.hee.tis.usermanagement.DTOs.UserDTO;

class HeeUserMapperTest {

  private static final String GMC_ID = "1234567";
  private static final String FIRST_NAME = "Anthony";
  private static final String LAST_NAME = "Gilliam";
  private static final String PHONE_NUMBER = "0123456789";
  private static final String EMAIL = "anthony.gilliam@dummy.com";
  private static final Set<String> DBCS = Set.of("DB1", "DB2");

  private static final String SUB = "5e5axxxx-e1xx-4axx-xxxx-96a7f86xxxxx";
  private static final String PREFERRED_MFA = "SOFTWARE_TOKEN_MFA";
  private static final List<String> MFA_SETTINGS = Arrays.asList("EMAIL_OTP", "SOFTWARE_TOKEN_MFA");

  private static final String ROLE_ADMIN = "ADMIN";
  private static final Long TRUST_ID = 100L;
  private static final Long PROGRAMME_ID = 200L;

  private HeeUserMapper heeUserMapper;

  @BeforeEach
  void setUp() {
    heeUserMapper = new HeeUserMapper();
  }

  @Test
  void shouldConvertHeeAndAuthUserCorrectly() {
    // given: HeeUserDTO
    HeeUserDTO heeUser = new HeeUserDTO();
    heeUser.setName(EMAIL);
    heeUser.setFirstName(FIRST_NAME);
    heeUser.setLastName(LAST_NAME);
    heeUser.setGmcId(GMC_ID);
    heeUser.setPhoneNumber(PHONE_NUMBER);
    heeUser.setEmailAddress(EMAIL);
    heeUser.setActive(true);
    heeUser.setDesignatedBodyCodes(DBCS);

    RoleDTO role = new RoleDTO();
    role.setName(ROLE_ADMIN);
    heeUser.setRoles(Set.of(role));

    UserTrustDTO trust = new UserTrustDTO();
    trust.setTrustId(TRUST_ID);
    heeUser.setAssociatedTrusts(Set.of(trust));

    UserProgrammeDTO programme = new UserProgrammeDTO();
    programme.setProgrammeId(PROGRAMME_ID);
    heeUser.setAssociatedProgrammes(Set.of(programme));

    // given: AuthenticationUserDto
    AuthenticationUserDto authUser = new AuthenticationUserDto();
    authUser.setId(SUB);
    authUser.setEnabled(true);
    authUser.setPreferredMfaSetting(PREFERRED_MFA);
    authUser.setUserMfaSettingList(MFA_SETTINGS);

    // when
    UserDTO result = heeUserMapper.convert(heeUser, authUser);

    // then: HeeUser mapping
    assertEquals(EMAIL, result.getName());
    assertEquals(FIRST_NAME, result.getFirstName());
    assertEquals(LAST_NAME, result.getLastName());
    assertEquals(GMC_ID, result.getGmcId());
    assertEquals(PHONE_NUMBER, result.getPhoneNumber());
    assertEquals(EMAIL, result.getEmailAddress());
    assertEquals(DBCS, result.getLocalOffices());
    assertEquals(ROLE_ADMIN, result.getRoles().iterator().next());
    assertEquals(TRUST_ID.toString(), result.getAssociatedTrusts().iterator().next());
    assertEquals(PROGRAMME_ID.toString(), result.getAssociatedProgrammes().iterator().next());

    // then: AuthUser mapping
    assertEquals(SUB, result.getAuthId());
    assertTrue(result.isActive());
    assertEquals(PREFERRED_MFA, result.getPreferredMfaSetting());
    assertEquals(MFA_SETTINGS, result.getUserMfaSettingList());
  }

  @Test
  void shouldHandleNullHeeUser() {
    AuthenticationUserDto authUser = new AuthenticationUserDto();
    authUser.setId(SUB);
    authUser.setEnabled(true);

    UserDTO result = heeUserMapper.convert(null, authUser);

    assertEquals(SUB, result.getAuthId());
    assertTrue(result.isActive());
  }

  @Test
  void shouldHandleNullAuthUser() {
    HeeUserDTO heeUser = new HeeUserDTO();
    heeUser.setFirstName(FIRST_NAME);
    heeUser.setActive(true);

    UserDTO result = heeUserMapper.convert(heeUser, null);

    assertEquals(FIRST_NAME, result.getFirstName());
    assertNull(result.getAuthId());
    assertTrue(result.isActive());
  }

  @Test
  void shouldHandleEmptyCollections() {
    HeeUserDTO heeUser = new HeeUserDTO();
    heeUser.setRoles(Set.of());
    heeUser.setAssociatedTrusts(Set.of());
    heeUser.setAssociatedProgrammes(Set.of());

    UserDTO result = heeUserMapper.convert(heeUser, null);

    assertTrue(result.getRoles().isEmpty());
    assertTrue(result.getAssociatedTrusts().isEmpty());
    assertTrue(result.getAssociatedProgrammes().isEmpty());
  }
}
