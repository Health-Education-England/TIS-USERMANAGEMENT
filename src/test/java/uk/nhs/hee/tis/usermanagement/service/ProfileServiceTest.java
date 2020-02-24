package uk.nhs.hee.tis.usermanagement.service;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.nhs.hee.tis.usermanagement.service.ProfileService.HEE_USERS_ENDPOINT;

import com.google.common.collect.Sets;
import com.transformuk.hee.tis.profile.client.service.impl.ProfileServiceImpl;
import com.transformuk.hee.tis.profile.dto.RoleDTO;
import com.transformuk.hee.tis.profile.dto.OrganisationalEntityDTO;
import com.transformuk.hee.tis.profile.service.dto.HeeUserDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import uk.nhs.hee.tis.usermanagement.event.CreateProfileUserRequestedEvent;
import uk.nhs.hee.tis.usermanagement.event.DeleteProfileUserRequestEvent;

@RunWith(MockitoJUnitRunner.class)
public class ProfileServiceTest {

  private static final String USERNAME = "username";
  private static final String NON_EXISTING_USER = "NON EXISTING USER";
  private static final String FIRST_NAME = "FIRST NAME";
  private static final String LAST_NAME = "LAST NAME";
  private static final String NAME = "NAME";
  private static final boolean ACTIVE = true;
  private static final String EMAIL_ADDRESS = "EMAIL ADDRESS";
  private static final String GMC_ID = "1234567";
  private static final String PASSWORD = "PASSWORD";
  private static final boolean TEMPORARY_PASSWORD = false;
  private static final String ROLE_NAME_1 = "ROLE NAME 1";
  private static final String ROLE_NAME_2 = "ROLE NAME 2";
  private static final String ROLE_NAME_3 = "ROLE NAME 3";
  private static final String ENTITY_NAME_1 = "ENTITY NAME 1";
  private static final String ENTITY_NAME_2 = "ENTITY NAME 2";

  @InjectMocks
  private ProfileService testObj;

  @Mock
  private ProfileServiceImpl profileServiceImplMock;

  @Mock
  private RestTemplate profileRestTemplateMock;

  @Captor
  private ArgumentCaptor<String> urlArgumentCaptor;

  @Captor
  private ArgumentCaptor<ParameterizedTypeReference<List<RoleDTO>>> typeReferenceArgumentCaptor;

  @Captor
  private ArgumentCaptor<ParameterizedTypeReference<List<OrganisationalEntityDTO>>> entityTypeReferenceArgumentCaptor;

  @Test(expected = NullPointerException.class)
  public void getAllUsersShouldThrowExceptionWhenPageableParameterIsNull() {
    try {
      testObj.getAllUsers(null, "SEARCH STRING");
    } finally {
      verifyZeroInteractions(profileServiceImplMock);
    }
  }

  @Test
  public void getAllUsersShouldReturnEmptyPageWhenProfileServiceIsDown() {
    PageRequest pageRequest = PageRequest.of(0, 10);

    doThrow(new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE))
        .when(profileServiceImplMock).getAllAdminUsers(pageRequest, USERNAME);

    Page<HeeUserDTO> result = testObj.getAllUsers(pageRequest, USERNAME);

    Assert.assertEquals(0, result.getContent().size());
  }

  @Test
  public void getAllUsersShouldReturnEmptyPageWhenProfileServiceThrowsAnException() {
    PageRequest pageRequest = PageRequest.of(0, 10);

    doThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR))
        .when(profileServiceImplMock).getAllAdminUsers(pageRequest, USERNAME);

    Page<HeeUserDTO> result = testObj.getAllUsers(pageRequest, USERNAME);

    Assert.assertEquals(0, result.getContent().size());
  }

  @Test
  public void getAllUsersShouldCallProfileServiceClientToGetPaginatedUsers() {
    PageRequest pageRequest = PageRequest.of(0, 10);
    HeeUserDTO heeUserDTO = new HeeUserDTO();
    heeUserDTO.setName("usern");
    PageImpl<HeeUserDTO> pagedHeeUsers = new PageImpl<>(Lists.newArrayList(heeUserDTO));

    when(profileServiceImplMock.getAllAdminUsers(pageRequest, USERNAME)).thenReturn(pagedHeeUsers);

    Page<HeeUserDTO> result = testObj.getAllUsers(pageRequest, USERNAME);

    Assert.assertSame(pagedHeeUsers, result);
    verify(profileServiceImplMock).getAllAdminUsers(pageRequest, USERNAME);
  }

  @Test(expected = NullPointerException.class)
  public void getUserByUsernameShouldThrowExceptionWhenUsernameIsNull() {
    try {
      testObj.getUserByUsername(null);
    } finally {
      verifyZeroInteractions(profileServiceImplMock);
    }
  }

  @Test
  public void getUsernameShouldReturnEmptyOptionalWhenUserNotFound() {
    doThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND)).when(profileServiceImplMock)
        .getSingleAdminUser(NON_EXISTING_USER);

    Optional<HeeUserDTO> result = testObj.getUserByUsername(NON_EXISTING_USER);

    Assert.assertFalse(result.isPresent());
  }

  @Test
  public void getUsernameShouldReturnEmptyOptionalWhenServerSideErrorOccurs() {
    doThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR))
        .when(profileServiceImplMock).getSingleAdminUser(USERNAME);

    Optional<HeeUserDTO> result = testObj.getUserByUsername(USERNAME);

    Assert.assertFalse(result.isPresent());
  }

  @Test
  public void getUsernameShouldReturnUserInOptionalWhenFound() {
    HeeUserDTO foundUser = new HeeUserDTO();
    when(profileServiceImplMock.getSingleAdminUser(USERNAME)).thenReturn(foundUser);

    Optional<HeeUserDTO> result = testObj.getUserByUsername(USERNAME);
    Assert.assertTrue(result.isPresent());
    Assert.assertSame(foundUser, result.get());
  }

  @Test(expected = NullPointerException.class)
  public void createProfileUserEventListenerShouldThrowExceptionWhenUserIsNull() {
    try {
      testObj.createProfileUserEventListener(null);
    } finally {
      verifyZeroInteractions(profileServiceImplMock);
    }
  }


  @Test
  public void createUserShouldCallProfileServiceToCreateUser() {
    HeeUserDTO userToCreateDTO = createHeeUserDto();
    when(profileServiceImplMock.createDto(userToCreateDTO, HEE_USERS_ENDPOINT, HeeUserDTO.class))
        .thenReturn(userToCreateDTO);

    testObj
        .createProfileUserEventListener(new CreateProfileUserRequestedEvent(userToCreateDTO, null));

    verify(profileServiceImplMock).createDto(userToCreateDTO, HEE_USERS_ENDPOINT, HeeUserDTO.class);
  }


  @Test(expected = NullPointerException.class)
  public void updateUserShouldThrowExceptionWhenUserIsNull() {
    try {
      testObj.updateUser(null);
    } finally {
      verifyZeroInteractions(profileServiceImplMock);
    }
  }


  @Test
  public void updateUserShouldReturnNewUserWhenUserDoesNotExist() {
    HeeUserDTO userToUpdateDTO = createHeeUserDto();
    when(profileServiceImplMock.updateDto(userToUpdateDTO, HEE_USERS_ENDPOINT, HeeUserDTO.class))
        .thenReturn(userToUpdateDTO);

    Optional<HeeUserDTO> result = testObj.updateUser(userToUpdateDTO);

    Assert.assertTrue(result.isPresent());
    verify(profileServiceImplMock).updateDto(userToUpdateDTO, HEE_USERS_ENDPOINT, HeeUserDTO.class);
  }


  @Test
  public void updateUserShouldReturnEmptyOptionalWhenServiceIsDown() {
    HeeUserDTO userToUpdateDTO = createHeeUserDto();

    doThrow(new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE))
        .when(profileServiceImplMock)
        .updateDto(userToUpdateDTO, HEE_USERS_ENDPOINT, HeeUserDTO.class);
    Optional<HeeUserDTO> result = testObj.updateUser(userToUpdateDTO);

    Assert.assertFalse(result.isPresent());
    verify(profileServiceImplMock).updateDto(userToUpdateDTO, HEE_USERS_ENDPOINT, HeeUserDTO.class);
  }

  @Test
  public void updateUserShouldReturnEmptyOptionalWhenServiceErrors() {
    HeeUserDTO userToUpdateDTO = createHeeUserDto();

    doThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR))
        .when(profileServiceImplMock)
        .updateDto(userToUpdateDTO, HEE_USERS_ENDPOINT, HeeUserDTO.class);
    Optional<HeeUserDTO> result = testObj.updateUser(userToUpdateDTO);

    Assert.assertFalse(result.isPresent());
    verify(profileServiceImplMock).updateDto(userToUpdateDTO, HEE_USERS_ENDPOINT, HeeUserDTO.class);
  }

  @Test
  public void updateUserShouldReturnEmptyOptionalWhenFailsValidation() {
    HeeUserDTO userToUpdateDTO = createHeeUserDto();

    doThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST)).when(profileServiceImplMock)
        .updateDto(userToUpdateDTO, HEE_USERS_ENDPOINT, HeeUserDTO.class);
    Optional<HeeUserDTO> result = testObj.updateUser(userToUpdateDTO);

    Assert.assertFalse(result.isPresent());
    verify(profileServiceImplMock).updateDto(userToUpdateDTO, HEE_USERS_ENDPOINT, HeeUserDTO.class);
  }

  @Test
  public void updateUserShouldReturnUpdatedModelOfUserAfterUpdate() {
    HeeUserDTO userToUpdateDTO = createHeeUserDto();

    when(profileServiceImplMock.updateDto(userToUpdateDTO, HEE_USERS_ENDPOINT, HeeUserDTO.class))
        .thenReturn(userToUpdateDTO);

    Optional<HeeUserDTO> result = testObj.updateUser(userToUpdateDTO);

    Assert.assertTrue(result.isPresent());
    verify(profileServiceImplMock).updateDto(userToUpdateDTO, HEE_USERS_ENDPOINT, HeeUserDTO.class);
  }

  @Test
  public void getAllRolesShouldReturnAllRolesFromProfileService() {
    RoleDTO roleDTO1 = new RoleDTO();
    roleDTO1.setName(ROLE_NAME_1);
    roleDTO1.setPermissions(Sets.newHashSet());
    RoleDTO roleDTO2 = new RoleDTO();
    roleDTO2.setName(ROLE_NAME_2);
    roleDTO2.setPermissions(Sets.newHashSet());
    RoleDTO roleDTO3 = new RoleDTO();
    roleDTO3.setName(ROLE_NAME_3);
    roleDTO3.setPermissions(Sets.newHashSet());
    ArrayList<RoleDTO> rolesList = Lists.newArrayList(roleDTO1, roleDTO2, roleDTO3);

    when(profileRestTemplateMock.exchange(urlArgumentCaptor.capture(), eq(HttpMethod.GET), eq(null),
        typeReferenceArgumentCaptor.capture())).thenReturn(ResponseEntity.ok(rolesList));
    ReflectionTestUtils.setField(testObj, "serviceUrl", "http://profileUrl.com");

    List<String> result = testObj.getAllRoles();

    Assert.assertEquals(3, result.size());
    Assert
        .assertTrue(result.containsAll(Lists.newArrayList(ROLE_NAME_1, ROLE_NAME_2, ROLE_NAME_3)));
  }

  @Test
  public void getAllRolesShouldReturnNoRolesFromProfileServiceWhenProfileFails() {
    when(profileRestTemplateMock.exchange(urlArgumentCaptor.capture(), eq(HttpMethod.GET), eq(null),
        typeReferenceArgumentCaptor.capture()))
        .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
    ReflectionTestUtils.setField(testObj, "serviceUrl", "http://profileUrl.com");

    List<String> result = testObj.getAllRoles();

    Assert.assertEquals(0, result.size());
  }


  @Test
  public void deleteUserEventListenerShouldDeleteUserWhenEventTriggered() {
    when(profileServiceImplMock.deleteUser(USERNAME)).thenReturn(true);

    testObj.deleteUserEventListener(new DeleteProfileUserRequestEvent(USERNAME));

    verify(profileServiceImplMock).deleteUser(USERNAME);
  }

  /**
   * Test that a NullPointerException is thrown when the username is null.
   */
  @Test(expected = NullPointerException.class)
  public void testGetUserByUsernameIgnoreCase_null_nullPointerException() {
    // Call the code under test.
    testObj.getUserByUsernameIgnoreCase(null);
  }

  @Test
  public void getAllEntitiesShouldReturnAllEntitiesFromProfileService() {
    OrganisationalEntityDTO entityDTO1 = new OrganisationalEntityDTO();
    entityDTO1.setName(ENTITY_NAME_1);
    OrganisationalEntityDTO entityDTO2 = new OrganisationalEntityDTO();
    entityDTO2.setName(ENTITY_NAME_2);
    ArrayList<OrganisationalEntityDTO> entitiesList = Lists.newArrayList(entityDTO1, entityDTO2);

    when(profileRestTemplateMock.exchange(urlArgumentCaptor.capture(), eq(HttpMethod.GET), eq(null),
            entityTypeReferenceArgumentCaptor.capture())).thenReturn(ResponseEntity.ok(entitiesList));
    ReflectionTestUtils.setField(testObj, "serviceUrl", "http://profileUrl.com");

    List<String> result = testObj.getAllEntities();

    Assert.assertEquals(2, result.size());
    Assert.assertTrue(result.containsAll(Lists.newArrayList(ENTITY_NAME_1, ENTITY_NAME_2)));
  }

  @Test
  public void getAllEntitiesShouldReturnNoEntitiesFromProfileServiceWhenProfileFails() {
    when(profileRestTemplateMock.exchange(urlArgumentCaptor.capture(), eq(HttpMethod.GET), eq(null),
            entityTypeReferenceArgumentCaptor.capture()))
            .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
    ReflectionTestUtils.setField(testObj, "serviceUrl", "http://profileUrl.com");

    List<String> result = testObj.getAllEntities();

    Assert.assertEquals(0, result.size());
  }

  private HeeUserDTO createHeeUserDto() {
    HeeUserDTO userToCreateDTO = new HeeUserDTO();
    userToCreateDTO.setFirstName(FIRST_NAME);
    userToCreateDTO.setLastName(LAST_NAME);
    userToCreateDTO.setName(NAME);
    userToCreateDTO.setActive(ACTIVE);
    userToCreateDTO.setEmailAddress(EMAIL_ADDRESS);
    userToCreateDTO.setGmcId(GMC_ID);
    userToCreateDTO.setPassword(PASSWORD);
    userToCreateDTO.setTemporaryPassword(TEMPORARY_PASSWORD);
    userToCreateDTO.setDesignatedBodyCodes(Sets.newHashSet());
    return userToCreateDTO;
  }
}
