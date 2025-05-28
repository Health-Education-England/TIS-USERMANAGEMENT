package uk.nhs.hee.tis.usermanagement.service;

import java.util.List;
import java.util.Optional;
import uk.nhs.hee.tis.usermanagement.DTOs.AuthenticationUserDto;
import uk.nhs.hee.tis.usermanagement.DTOs.UserAuthEventDto;
import uk.nhs.hee.tis.usermanagement.DTOs.UserDTO;

/**
 * An interface for performing admin actions against the implementing authentication provider.
 */
public interface AuthenticationAdminService {

  /**
   * Get the name of the authentication provider service.
   *
   * @return The service name.
   */
  String getServiceName();

  /**
   * Get the user from the authentication provider for the given username.
   *
   * @param username The username to get the user for.
   * @return An optional user, empty if not found.
   */
  Optional<AuthenticationUserDto> getUser(String username);

  /**
   * Update the user in the authentication provider.
   *
   * @param authenticationUser The user details to update.
   * @return Whether the update was successful.
   */
  boolean updateUser(AuthenticationUserDto authenticationUser);

  /**
   * Update the user in the authentication provider.
   *
   * @param userDto The user details to update.
   * @return Whether the update was successful.
   */
  boolean updateUser(UserDTO userDto);

  /**
   * Update the password for the given user.
   *
   * @param userId       The id of the user to update.
   * @param password     The new password.
   * @param tempPassword Whether the password is temporary or permanent.
   * @return Whether the update was successful.
   */
  boolean updatePassword(String userId, String password, boolean tempPassword);

  /**
   * Get authentication event logs for user.
   *
   * @param username The username to get the auth event logs for.
   */
  List<UserAuthEventDto> getUserAuthEvents(String username);
}
