package uk.nhs.hee.tis.usermanagement.service;

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

  @Test
  public void getUsernameShouldReturnEmptyOptionalWhenUserNotFound() {
    doThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND)).when(profileServiceImplMock).getSingleAdminUser(NON_EXISTING_USER);

    Optional<HeeUserDTO> result = testObj.getUserByUsername(NON_EXISTING_USER);

    Assert.assertFalse(result.isPresent());
  }

  @Test
  public void getUsernameShouldReturnEmptyOptionalWhenServerSideErrorOccurs() {
    doThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR)).when(profileServiceImplMock).getSingleAdminUser(USERNAME);

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
}