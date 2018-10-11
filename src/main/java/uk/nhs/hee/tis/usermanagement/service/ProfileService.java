package uk.nhs.hee.tis.usermanagement.service;

import com.google.common.base.Preconditions;
import com.transformuk.hee.tis.profile.client.service.impl.ProfileServiceImpl;
import com.transformuk.hee.tis.profile.service.dto.HeeUserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.nhs.hee.tis.usermanagement.command.profile.CreateUserCommand;
import uk.nhs.hee.tis.usermanagement.command.profile.GetAllRolesCommand;
import uk.nhs.hee.tis.usermanagement.command.profile.GetPaginatedUsersCommand;
import uk.nhs.hee.tis.usermanagement.command.profile.GetUserByUsernameCommand;
import uk.nhs.hee.tis.usermanagement.command.profile.UpdateUserCommand;

import java.util.List;
import java.util.Optional;

@Service
public class ProfileService {

  private static final Logger LOG = LoggerFactory.getLogger(ProfileService.class);
  protected static final String HEE_USERS_ENDPOINT = "/api/hee-users";

  @Autowired
  private ProfileServiceImpl profileServiceImpl;
  @Autowired
  private RestTemplate profileRestTemplate;
  @Value("${profile.service.url}")
  private String serviceUrl;

  /**
   * Get all users in a paginated form. If a username is provideded, a (sql) like search is done.
   *
   * @param pageable pageable object defining the the page as well a size required
   * @param username nullable username, if provided does the like search, if null then empty search
   * @return Page of HeeUserDTO
   */
  public Page<HeeUserDTO> getAllUsers(Pageable pageable, @Nullable String username) {
    Preconditions.checkNotNull(pageable, "Pageable cannot be null when requesting a paginated result");

    GetPaginatedUsersCommand getPaginatedUsersCommand = new GetPaginatedUsersCommand(profileServiceImpl, pageable, username);
    return getPaginatedUsersCommand.execute();
  }

  /**
   * Get a single user by username
   *
   * @param username the username to search by
   * @return an optional of the found user
   */
  public Optional<HeeUserDTO> getUserByUsername(String username) {
    Preconditions.checkNotNull(username, "Username cannot be null when searching for a user by username");

    GetUserByUsernameCommand getUserByUsernameCommand = new GetUserByUsernameCommand(profileServiceImpl, username);
    return getUserByUsernameCommand.execute();
  }

  /**
   * Create a user in the profile service
   *
   * @param userToCreateDTO the new user details
   * @return an optional HeeUserDTO that represents the user in the profile service
   */
  public Optional<HeeUserDTO> createUser(HeeUserDTO userToCreateDTO) {
    Preconditions.checkNotNull(userToCreateDTO, "Cannot create user if user is null");

    CreateUserCommand createUserCommand = new CreateUserCommand(profileServiceImpl, userToCreateDTO);
    return createUserCommand.execute();
  }

  /**
   * Update a user in the profile service
   *
   * @param userToUpdateDTO the state of the user details to update to
   * @return an optional of the HeeUserDTO that represents the user in the profile service
   */
  public Optional<HeeUserDTO> updateUser(HeeUserDTO userToUpdateDTO) {
    Preconditions.checkNotNull(userToUpdateDTO, "Cannot update user if user to update is null");

    UpdateUserCommand updateUserCommand = new UpdateUserCommand(profileServiceImpl, userToUpdateDTO);
    return updateUserCommand.execute();
  }

  /**
   * YOLO
   *
   * @return
   */
  public List<String> getAllRoles() {
    GetAllRolesCommand getAllRolesCommand = new GetAllRolesCommand(profileRestTemplate, serviceUrl);
    return getAllRolesCommand.execute();
  }


}
