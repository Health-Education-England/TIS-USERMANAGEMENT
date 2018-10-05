package uk.nhs.hee.tis.usermanagement.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.transformuk.hee.tis.profile.client.service.impl.ProfileServiceImpl;
import com.transformuk.hee.tis.profile.service.dto.HeeUserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.util.Optional;

@Service
public class ProfileService {

  private static final Logger LOG = LoggerFactory.getLogger(ProfileService.class);
  protected static final String HEE_USERS_ENDPOINT = "/api/hee-users";

  @Autowired
  private ProfileServiceImpl profileServiceImpl;

  /**
   * Get all users in a paginated form. If a username is provideded, a (sql) like search is done.
   *
   * @param pageable pageable object defining the the page as well a size required
   * @param username nullable username, if provided does the like search, if null then empty search
   * @return Page of HeeUserDTO
   */
  public Page<HeeUserDTO> getAllUsers(Pageable pageable, @Nullable String username) {
    Preconditions.checkNotNull(pageable, "Pageable cannot be null when requesting a paginated result");
    try {
      Page<HeeUserDTO> heeUserDTOS = profileServiceImpl.getAllAdminUsers(pageable, username);
      return heeUserDTOS;
    } catch (HttpServerErrorException e) {
      LOG.warn("A server error occurred while getting a page of users from profile service, returning an empty page of users as fallback");
      e.printStackTrace();
      return new PageImpl<>(Lists.newArrayList());
    }
  }

  public HeeUserDTO getUserByUsername(String username) {
    Preconditions.checkNotNull(username, "Username cannot be null when searching for a user by username");

    HeeUserDTO heeUserDTO = profileServiceImpl.getSingleAdminUser(username);
    return heeUserDTO;
  }

  public Optional<HeeUserDTO> createUser(HeeUserDTO userToCreateDTO) {
    Preconditions.checkNotNull(userToCreateDTO, "Cannot create user if user is null");

    try {
      HeeUserDTO heeUserDTO = profileServiceImpl.createDto(userToCreateDTO, HEE_USERS_ENDPOINT, HeeUserDTO.class);
      return Optional.ofNullable(heeUserDTO);
    } catch (HttpServerErrorException e) {
      LOG.warn("A server error occurred while attempting to create a hee user in profile service, returning an empty optional as fallback");
      e.printStackTrace();
      return Optional.empty();
    } catch (HttpClientErrorException e) {
      LOG.warn("A client exception thrown while attempting to create a hee user in profile service, returning an empty optional as fallback");
      e.printStackTrace();
      return Optional.empty();
    }
  }

  public Optional<HeeUserDTO> updateUser(HeeUserDTO userToUpdateDTO) {
    Preconditions.checkNotNull(userToUpdateDTO, "Cannot update user if user to update is null");
    try {
      HeeUserDTO heeUserDTO = profileServiceImpl.updateDto(userToUpdateDTO, HEE_USERS_ENDPOINT, HeeUserDTO.class);
      return Optional.ofNullable(heeUserDTO);
    } catch (HttpServerErrorException e) {
      LOG.warn("A server error occurred while trying to update a user in profile service, returning empty optional as fallback");
      e.printStackTrace();
      return Optional.empty();
    } catch (HttpClientErrorException e) {
      LOG.warn("A client error occurred while trying to update a user in profile service, returning empty optional as fallback");
      e.printStackTrace();
      return Optional.empty();
    }
  }
}
