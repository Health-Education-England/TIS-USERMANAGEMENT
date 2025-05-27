package uk.nhs.hee.tis.usermanagement.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import uk.nhs.hee.tis.usermanagement.DTOs.AuthenticationUserDto;
import uk.nhs.hee.tis.usermanagement.DTOs.CreateUserDTO;
import uk.nhs.hee.tis.usermanagement.event.CreateAuthenticationUserRequestedEvent;
import uk.nhs.hee.tis.usermanagement.event.CreateProfileUserRequestedEvent;
import uk.nhs.hee.tis.usermanagement.event.DeleteAuthenticationUserRequestedEvent;
import uk.nhs.hee.tis.usermanagement.event.DeleteProfileUserRequestEvent;

/**
 * An abstract class containing behaviour common across different authentication providers.
 */
@Slf4j
public abstract class AbstractAuthenticationAdminService implements AuthenticationAdminService {

  private final ApplicationEventPublisher applicationEventPublisher;

  AbstractAuthenticationAdminService(ApplicationEventPublisher applicationEventPublisher) {
    this.applicationEventPublisher = applicationEventPublisher;
  }

  /**
   * Create the user in the authentication provider.
   *
   * @param createUserDto The user details.
   * @return The created user.
   */
  abstract AuthenticationUserDto createUser(CreateUserDTO createUserDto);

  /**
   * Delete the user from the authentication provider.
   *
   * @param authenticationUser The user to delete.
   */
  abstract void deleteUser(AuthenticationUserDto authenticationUser);

  /**
   * Create user in the authentication provider and publish profile create event.
   *
   * @param event Event containing the user to create.
   */
  @EventListener
  public void createUserEventListener(CreateAuthenticationUserRequestedEvent event) {
    // create user in KeyCloak
    log.info("Received CreateAuthenticationUserRequestedEvent for user [{}]",
        event.getUserDTO().getEmailAddress());
    AuthenticationUserDto authenticationUser = createUser(event.getUserDTO());
    applicationEventPublisher.publishEvent(
        new CreateProfileUserRequestedEvent(event.getUserToCreateInProfileService(),
            authenticationUser));
  }

  /**
   * Delete user in the authentication provider and conditionally publish profile delete event.
   *
   * @param event Event containing the user to delete.
   */
  @EventListener
  public void deleteKeycloakUserEventListener(DeleteAuthenticationUserRequestedEvent event) {
    AuthenticationUserDto authenticationUser = event.getAuthenticationUser();
    log.info("Received DeleteAuthenticationUserEvent for user [{}]",
        authenticationUser.getUsername());
    deleteUser(authenticationUser);

    if (event.isPublishDeleteProfileUserEvent()) {
      applicationEventPublisher.publishEvent(
          new DeleteProfileUserRequestEvent(authenticationUser.getUsername()));
    }
  }
}
