package uk.nhs.hee.tis.usermanagement.service;

import com.google.common.collect.Sets;
import com.transformuk.hee.tis.profile.client.service.impl.ProfileServiceImpl;
import com.transformuk.hee.tis.profile.service.dto.HeeUserDTO;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.util.Optional;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProfileServiceTest {

  private static final String USERNAME = "username";
  private static final String NON_EXISTING_USER = "NON EXISTING USER";
  public static final String FIRST_NAME = "FIRST NAME";
  public static final String LAST_NAME = "LAST NAME";
  public static final String NAME = "NAME";
  public static final boolean ACTIVE = true;
  public static final String EMAIL_ADDRESS = "EMAIL ADDRESS";
  public static final String GMC_ID = "1234567";
  public static final String PASSWORD = "PASSWORD";
  public static final boolean TEMPORARY_PASSWORD = false;
  public static final String URL = "URL";

  @InjectMocks
  private ProfileService testObj;

  @Mock
  private ProfileServiceImpl profileServiceImplMock;

  @Test(expected = NullPointerException.class)
  public void getAllUsersShouldThrowExceptionWhenPageableParameterIsNull() {
    try {
      testObj.getAllUsers(null, "SEARCH STRING");
    } finally {
      verifyZeroInteractions(profileServiceImplMock);
    }
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

  //TODO: Uncomment the bellow when we refactor the getUserByUsername method
//  @Test
//  public void getUsernameShouldReturnEmptyOptionalWhenUserNotFound() {
//    doThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND)).when(profileServiceImplMock).getSingleAdminUser(NON_EXISTING_USER);
//
//    Optional<HeeUserDTO> result = testObj.getUserByUsername(NON_EXISTING_USER);
//
//    Assert.assertFalse(result.isPresent());
//  }
//
//  @Test
//  public void getUsernameShouldReturnEmptyOptionalWhenServerSideErrorOccurs() {
//    doThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR)).when(profileServiceImplMock).getSingleAdminUser(USERNAME);
//
//    Optional<HeeUserDTO> result = testObj.getUserByUsername(USERNAME);
//
//    Assert.assertFalse(result.isPresent());
//  }
//
//  @Test
//  public void getUsernameShouldReturnUserInOptionalWhenFound() {
//    HeeUserDTO foundUser = new HeeUserDTO();
//    when(profileServiceImplMock.getSingleAdminUser(USERNAME)).thenReturn(foundUser);
//
//    Optional<HeeUserDTO> result = testObj.getUserByUsername(USERNAME);
//    Assert.assertTrue(result.isPresent());
//    Assert.assertSame(foundUser, result.get());
//  }

  @Test(expected = NullPointerException.class)
  public void createUserShouldThrowExceptionWhenUserIsNull() {
    try {
      testObj.createUser(null);
    } finally {
      verifyZeroInteractions(profileServiceImplMock);
    }
  }


  @Test
  public void createUserShouldReturnEmptyOptionalWhenProfileServiceErrors() {
    HeeUserDTO userToCreateDTO = createHeeUserDto();

    when(profileServiceImplMock.getServiceUrl()).thenReturn(URL);
    doThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR)).when(profileServiceImplMock).createDto(userToCreateDTO, URL, HeeUserDTO.class);
    Optional<HeeUserDTO> result = testObj.createUser(userToCreateDTO);

    Assert.assertFalse(result.isPresent());
    verify(profileServiceImplMock).createDto(userToCreateDTO, URL, HeeUserDTO.class);
  }

  @Test
  public void createUserShouldReturnEmptyOptionalWhenProfileServiceIsDown() {
    HeeUserDTO userToCreateDTO = createHeeUserDto();

    when(profileServiceImplMock.getServiceUrl()).thenReturn(URL);
    doThrow(new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE)).when(profileServiceImplMock).createDto(userToCreateDTO, URL, HeeUserDTO.class);
    Optional<HeeUserDTO> result = testObj.createUser(userToCreateDTO);

    Assert.assertFalse(result.isPresent());
    verify(profileServiceImplMock).createDto(userToCreateDTO, URL, HeeUserDTO.class);
  }

  @Test
  public void createUserShouldReturnEmptyOptionalWhenRequestFailsValidation() {
    HeeUserDTO userToCreateDTO = createHeeUserDto();

    when(profileServiceImplMock.getServiceUrl()).thenReturn(URL);
    doThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST)).when(profileServiceImplMock).createDto(userToCreateDTO, URL, HeeUserDTO.class);
    Optional<HeeUserDTO> result = testObj.createUser(userToCreateDTO);

    Assert.assertFalse(result.isPresent());
    verify(profileServiceImplMock).createDto(userToCreateDTO, URL, HeeUserDTO.class);
  }

  @Test
  public void createUserShouldCallProfileServiceToCreateUser() {
    HeeUserDTO userToCreateDTO = createHeeUserDto();

    Optional<HeeUserDTO> result = testObj.createUser(userToCreateDTO);

    verify(profileServiceImplMock).createDto(userToCreateDTO, profileServiceImplMock.getServiceUrl(), HeeUserDTO.class);

    Assert.assertTrue(result.isPresent());
    Assert.assertEquals(userToCreateDTO, result.get());
  }

  @Test
  public void updateUserShouldReturnEmptyOptionalWhenUserDoesNotExist() {
    HeeUserDTO userToUpdateDTO = createHeeUserDto();

    Optional<HeeUserDTO> result = testObj.updateUser(userToUpdateDTO);

    Assert.assertFalse(result.isPresent());
    verify(profileServiceImplMock).updateDto(userToUpdateDTO, profileServiceImplMock.getServiceUrl(), HeeUserDTO.class);
  }


  @Test
  public void updateUserShouldReturnEmptyOptionalWhenServiceIsDown() {
    HeeUserDTO userToUpdateDTO = createHeeUserDto();

    when(profileServiceImplMock.getServiceUrl()).thenReturn(URL);
    doThrow(new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE)).when(profileServiceImplMock).updateDto(userToUpdateDTO, URL, HeeUserDTO.class);
    Optional<HeeUserDTO> result = testObj.updateUser(userToUpdateDTO);

    Assert.assertFalse(result.isPresent());
    verify(profileServiceImplMock).updateDto(userToUpdateDTO, profileServiceImplMock.getServiceUrl(), HeeUserDTO.class);
  }

  @Test
  public void updateUserShouldReturnEmptyOptionalWhenServiceErrors() {
    HeeUserDTO userToUpdateDTO = createHeeUserDto();

    when(profileServiceImplMock.getServiceUrl()).thenReturn(URL);
    doThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR)).when(profileServiceImplMock).updateDto(userToUpdateDTO, URL, HeeUserDTO.class);
    Optional<HeeUserDTO> result = testObj.updateUser(userToUpdateDTO);

    Assert.assertFalse(result.isPresent());
    verify(profileServiceImplMock).updateDto(userToUpdateDTO, profileServiceImplMock.getServiceUrl(), HeeUserDTO.class);
  }

  @Test
  public void updateUserShouldReturnEmptyOptionalWhenFailsValidation() {
    HeeUserDTO userToUpdateDTO = createHeeUserDto();

    when(profileServiceImplMock.getServiceUrl()).thenReturn(URL);
    doThrow(new HttpServerErrorException(HttpStatus.BAD_REQUEST)).when(profileServiceImplMock).updateDto(userToUpdateDTO, URL, HeeUserDTO.class);
    Optional<HeeUserDTO> result = testObj.updateUser(userToUpdateDTO);

    Assert.assertFalse(result.isPresent());
    verify(profileServiceImplMock).updateDto(userToUpdateDTO, profileServiceImplMock.getServiceUrl(), HeeUserDTO.class);
  }

  @Test
  public void updateUserShouldReturnUpdatedModelOfUserAfterUpdate() {
    HeeUserDTO userToUpdateDTO = createHeeUserDto();

    when(profileServiceImplMock.getServiceUrl()).thenReturn(URL);
    when(profileServiceImplMock.updateDto(userToUpdateDTO, URL, HeeUserDTO.class)).thenReturn(userToUpdateDTO);

    Optional<HeeUserDTO> result = testObj.updateUser(userToUpdateDTO);

    Assert.assertFalse(result.isPresent());
    verify(profileServiceImplMock).updateDto(userToUpdateDTO, profileServiceImplMock.getServiceUrl(), HeeUserDTO.class);

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