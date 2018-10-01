package uk.nhs.hee.tis.usermanagement.service;

import com.transformuk.hee.tis.profile.client.service.impl.ProfileServiceImpl;
import com.transformuk.hee.tis.profile.service.dto.HeeUserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProfileService {

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
    Page<HeeUserDTO> heeUserDTOS = profileServiceImpl.getAllAdminUsers(pageable, username);
    return heeUserDTOS;
  }

  public HeeUserDTO getUserByUsername(String username) {
    HeeUserDTO heeUserDTO = profileServiceImpl.getSingleAdminUser(username);
    return heeUserDTO;
  }

  public Optional<HeeUserDTO> createUser(HeeUserDTO userToCreateDTO) {
    return null;
  }

  public Optional<HeeUserDTO> updateUser(HeeUserDTO userToUpdateDTO) {
    return null;
  }
}
