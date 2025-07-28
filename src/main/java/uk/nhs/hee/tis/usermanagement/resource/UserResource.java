/*
 * The MIT License (MIT)
 *
 * Copyright 2024 Crown Copyright (NHS England)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package uk.nhs.hee.tis.usermanagement.resource;

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;
import software.amazon.awssdk.services.ses.model.SesException;
import uk.nhs.hee.tis.usermanagement.DTOs.CreateUserDTO;
import uk.nhs.hee.tis.usermanagement.DTOs.UserAuthEventDto;
import uk.nhs.hee.tis.usermanagement.DTOs.UserDTO;
import uk.nhs.hee.tis.usermanagement.exception.UserCreationException;
import uk.nhs.hee.tis.usermanagement.facade.UserManagementFacade;
import uk.nhs.hee.tis.usermanagement.service.EmailService;

/**
 * Resource that exposes user functionality which is expected to be supported. It operates slightly
 * differently from the {@link UserManagementController} it will replace.
 *
 * <p>Exceptions are not handled explicitly so
 * {@link org.springframework.web.bind.annotation.ControllerAdvice} can map to a Response.
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserResource {

  private static final String ERROR_TYPE = "errorType";
  private static final String ERROR_CODE = "errorCode";
  private static final String MESSAGE = "message";

  private final UserManagementFacade userFacade;
  private final EmailService emailService;

  private final CreateUserValidator createUserValidator = new CreateUserValidator();

  public UserResource(UserManagementFacade userFacade, EmailService emailService) {
    this.userFacade = userFacade;
    this.emailService = emailService;
  }

  /**
   * Gets complete user information, combining different sources (backends).
   *
   * @param username The name of the user to look for
   * @return The complete, composite user information
   */
  @PreAuthorize("hasAuthority('heeuser:view')")
  @GetMapping("/{username}")
  public UserDTO getCompleteUser(@PathVariable String username) {
    return userFacade.getCompleteUser(username);
  }

  /**
   * Gets a list of users, matching a search term if one is provided.
   *
   * @param pageable Definition of the page to return
   * @param search   Search term to search for in usernames
   * @return A collection of users which may be a page subset of the full results
   */
  @PreAuthorize("hasAuthority('heeuser:view')")
  @GetMapping
  public List<UserDTO> getAllUsers(Pageable pageable,
      @RequestParam(required = false) String search) {
    Page<UserDTO> page = userFacade.getAllUsers(pageable, search);
    return page.getContent();
  }

  /**
   * Creates a new user.
   *
   * @param user Complete information needed to create a user across backend services
   * @return The response entity indicates if the request was accepted but does not guarantee that
   *     the user was successfully created in all backends
   */
  @PreAuthorize("hasAuthority('heeuser:add:modify')")
  @PostMapping
  public ResponseEntity<CreateUserDTO> createUser(@RequestBody CreateUserDTO user) {
    createUserValidator.validate(user);
    userFacade.publishUserCreationRequestedEvent(user);
    return ResponseEntity.accepted().build();
  }

  /**
   * Updates an existing user.
   *
   * @param user     The full user details with the changes to save
   * @param username The username, which is expected to be the same as {@code  user.getName()}
   * @return The {@link ResponseEntity} indicating the request has been accepted and should be
   *     updated
   */
  @PreAuthorize("hasAuthority('heeuser:add:modify')")
  @PutMapping("/{username}")
  public ResponseEntity<UserDTO> updateUser(@RequestBody UserDTO user,
      @PathVariable String username) {
    userFacade.updateSingleUser(user);
    return ResponseEntity.accepted().build();
  }

  /**
   * Deletes an existing user.
   *
   * @param username The username of the user to delete
   * @return The {@link ResponseEntity} indicating the request has been accepted and should be
   *     deleted
   */
  @PreAuthorize("hasAuthority('heeuser:delete') and hasAuthority('profile:delete:entities') ")
  @DeleteMapping("/{username}")
  public ResponseEntity<UserDTO> deleteUser(@PathVariable String username) {
    userFacade.publishDeleteAuthenticationUserRequestedEvent(username);
    return ResponseEntity.accepted().build();
  }

  /**
   * Gets user auth event logs.
   *
   * @param username The name of the user to find auth event logs for
   * @return A list of auth event logs
   */
  @PreAuthorize("hasAuthority('heeuser:view')")
  @GetMapping("/{username}/authevents")
  public List<UserAuthEventDto> getUserAuthEventLogs(@PathVariable String username) {
    return userFacade.getUserAuthEvents(username);
  }

  /**
   * Trigger password reset.
   * The service will generate a random a password and send to the user's email.
   *
   * @param username the name of the user to reset password for
   * @return a map containing successful/failing messages
   */
  @PreAuthorize("hasAuthority('heeuser:add:modify')")
  @PostMapping("/{username}/trigger-password-reset")
  public ResponseEntity<Map<String, String>> triggerPasswordReset(@PathVariable String username) {
    try {
      String tempPwd = userFacade.triggerPasswordReset(username);
      emailService.sendTempPasswordEmail(username, tempPwd);
      return ResponseEntity.ok().body(
          Map.of(
              MESSAGE, "Password reset successfully"
          )
      );
    } catch (CognitoIdentityProviderException | SesException e) { // Capture Cognito exceptions
      String awsErrorCode = e.awsErrorDetails().errorCode(); // eg.UserNotFoundException
      String awsErrorMsg = e.awsErrorDetails().errorMessage();

      String errType;
      if (e instanceof CognitoIdentityProviderException) {
        errType = "Cognito Error";
      } else {
        errType = "SES Error";
      }
      log.error("{}: [{}] {}", errType, awsErrorCode, awsErrorMsg);

      return ResponseEntity.status(e.statusCode()).body(
          Map.of(
              ERROR_TYPE, errType,
              ERROR_CODE, awsErrorCode,
              MESSAGE, awsErrorMsg
          )
      );
    } catch (Exception e) {
      return ResponseEntity.status(500).body(
          Map.of(
              ERROR_TYPE, "Internal Server Error",
              ERROR_CODE, "Internal Server Error",
              MESSAGE, e.getMessage()
          )
      );
    }
  }

  /**
   * This validator has been created to encapsulate validation done in the
   * {@link UserManagementController}. A larger refactoring would involve modifying both to use a
   * validation service/bean.
   */
  private class CreateUserValidator {

    public void validate(CreateUserDTO user) {
      if (StringUtils.containsWhitespace(user.getName())) {
        throw new UserCreationException("Username must not contain whitespace.");
      }
      if (StringUtils.containsWhitespace(user.getEmailAddress())) {
        throw new UserCreationException("Email Address must not contain whitespace.");
      }
      UserDTO userDto = userFacade.getUserByNameIgnoreCase(user.getName());
      if (userDto != null) {
        throw new UserCreationException("Cannot create user because the username already exists.");
      }
    }
  }
}
