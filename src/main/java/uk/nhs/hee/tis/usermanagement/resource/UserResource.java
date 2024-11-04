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
import uk.nhs.hee.tis.usermanagement.DTOs.CreateUserDTO;
import uk.nhs.hee.tis.usermanagement.DTOs.UserDTO;
import uk.nhs.hee.tis.usermanagement.exception.UserCreationException;
import uk.nhs.hee.tis.usermanagement.facade.UserManagementFacade;

@RestController
@RequestMapping("/api/users")
public class UserResource {

  private final UserManagementFacade userFacade;

  private final CreateUserValidator createUserValidator = new CreateUserValidator();

  public UserResource(UserManagementFacade userFacade) {
    this.userFacade = userFacade;
  }

  @PreAuthorize("hasAuthority('heeuser:view')")
  @GetMapping("/{username}")
  public UserDTO getCompleteUser(@PathVariable String username) {
    return userFacade.getCompleteUser(username);
  }

  @PreAuthorize("hasAuthority('heeuser:view')")
  @GetMapping
  public List<UserDTO> getAllUsers(Pageable pageable,
      @RequestParam(required = false) String search) {
    Page<UserDTO> page = userFacade.getAllUsers(pageable, search);
    return page.getContent();
  }

  @PreAuthorize("hasAuthority('heeuser:add:modify')")
  @PostMapping
  public ResponseEntity<CreateUserDTO> createUser(@RequestBody CreateUserDTO user) {
    createUserValidator.validate(user);
    userFacade.publishUserCreationRequestedEvent(user);
    return ResponseEntity.accepted().build();
  }

  @PreAuthorize("hasAuthority('heeuser:add:modify')")
  @PutMapping("/{username}")
  public ResponseEntity<UserDTO> updateUser(@RequestBody UserDTO user,
      @PathVariable String username) {
    userFacade.updateSingleUser(user);
    return ResponseEntity.accepted().build();
  }

  @PreAuthorize("hasAuthority('heeuser:delete')")
  @DeleteMapping("/{username}")
  ResponseEntity<UserDTO> deleteUser(@PathVariable String username) {
    userFacade.publishDeleteAuthenticationUserRequestedEvent(username);
    return ResponseEntity.accepted().build();
  }

  /**
   * This validator has been created to encapsulate validation done in the {@link UserManagementController}.
   * A larger refactoring would involve modifying both to use a validation service/bean.
   */
  private class CreateUserValidator {

    public void validate(CreateUserDTO user) {
      if (StringUtils.containsWhitespace(user.getName())) {
        throw new UserCreationException("Username must not contain whitespace.");
      }
      if (StringUtils.containsWhitespace(user.getEmailAddress())) {
        throw new UserCreationException("Email Address must not contain whitespace.");
      }
      UserDTO userDTO = userFacade.getUserByNameIgnoreCase(user.getName());
      if (userDTO != null) {
        throw new UserCreationException("Cannot create user because the username already exists.");
      }
    }
  }
}
