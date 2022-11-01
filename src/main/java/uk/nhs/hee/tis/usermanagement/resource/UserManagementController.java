package uk.nhs.hee.tis.usermanagement.resource;

import com.transformuk.hee.tis.reference.api.dto.DBCDTO;
import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeDTO;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.nhs.hee.tis.usermanagement.DTOs.CreateUserDTO;
import uk.nhs.hee.tis.usermanagement.DTOs.UserDTO;
import uk.nhs.hee.tis.usermanagement.DTOs.UserPasswordDTO;
import uk.nhs.hee.tis.usermanagement.exception.PasswordException;
import uk.nhs.hee.tis.usermanagement.exception.UserCreationException;
import uk.nhs.hee.tis.usermanagement.facade.UserManagementFacade;

@Controller
public class UserManagementController {

  private static final String ATTRIBUTE_MESSAGE = "message";

  public static final int REQUIRED_PASSWORD_LENGTH = 8;
  @Autowired
  private UserManagementFacade userManagementFacade;

  @PreAuthorize("hasAuthority('heeuser:view')")
  @GetMapping("/user")
  public String getCompleteUser(@RequestParam String userName, Model model) {
    UserDTO completeUserDTO = userManagementFacade.getCompleteUser(userName);

    model.addAttribute("user", completeUserDTO);
    UserPasswordDTO userPasswordDTO = new UserPasswordDTO();
    userPasswordDTO.setKcId(completeUserDTO.getKcId());
    model.addAttribute("userPassword", userPasswordDTO);

    List<String> allRoles = userManagementFacade.getAllAssignableRoles();
    List<DBCDTO> allDBCs = userManagementFacade.getAllDBCs();
    List<TrustDTO> allTrusts = userManagementFacade.getAllTrusts();
    List<ProgrammeDTO> allProgrammes = userManagementFacade.getAllProgrammes();
    List<String> allEntityRoles = userManagementFacade.getAllEntityRoles();

    model.addAttribute("roles", allRoles);
    model.addAttribute("entityRoles", allEntityRoles);
    model.addAttribute("designatedBodyCodes", allDBCs);
    model.addAttribute("trusts", allTrusts);
    model.addAttribute("programmes", allProgrammes);
    return "userEdit";
  }

  @PreAuthorize("hasAuthority('heeuser:view')")
  @GetMapping("/allUsers")
  public String getAllUsers(@RequestParam(required = false, defaultValue = "") String search,
      @RequestParam(required = false, defaultValue = "0") int page,
      @RequestParam(required = false, defaultValue = "20") int size,
      Model model) {
    Pageable pageable = PageRequest.of(page, size);
    Page<UserDTO> userDTOS = userManagementFacade.getAllUsers(pageable, search);
    model.addAttribute("pagedUsers", userDTOS);
    model.addAttribute("currentPage", pageable.getPageNumber() + 1);
    model.addAttribute("searchParam", search);
    return "allUsers";
  }

  @PreAuthorize("hasAuthority('heeuser:view')")
  @GetMapping("/rolesForUsers")
  public String getRolesForUsers(@RequestParam(required = false, defaultValue = "") String search,
      @RequestParam(defaultValue = "") String sort,
      @RequestParam(defaultValue = "") String sortOrder,
      Model model) {
    List<UserDTO> userDtos = Arrays.stream(search.split("\\s"))
        .map(userManagementFacade::getUserByNameIgnoreCase)
        //        .sorted(Comparator.comparing())
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
    model.addAttribute("users", userDtos);
    return "rolesForUsers";
  }

  @PreAuthorize("hasAuthority('heeuser:add:modify')")
  @GetMapping("/createUser")
  public String viewCreateUser(Model model) {
    model.addAttribute("user", new CreateUserDTO());

    List<String> allRoles = userManagementFacade.getAllAssignableRoles();
    List<DBCDTO> allDBCs = userManagementFacade.getAllDBCs();
    List<TrustDTO> allTrusts = userManagementFacade.getAllTrusts();
    List<ProgrammeDTO> allProgrammes = userManagementFacade.getAllProgrammes();
    List<String> allEntityRoles = userManagementFacade.getAllEntityRoles();

    model.addAttribute("roles", allRoles);
    model.addAttribute("entityRoles", allEntityRoles);
    model.addAttribute("designatedBodyCodes", allDBCs);
    model.addAttribute("trusts", allTrusts);
    model.addAttribute("programmes", allProgrammes);

    return "createUser";
  }

  @PreAuthorize("hasAuthority('heeuser:add:modify')")
  @PostMapping("/createUser")
  public String createUser(@ModelAttribute CreateUserDTO user, Model model) {
    if (!StringUtils.equals(user.getPassword(), user.getConfirmPassword())) {
      throw new UserCreationException("Cannot create user, passwords do not match");
    } else if (StringUtils.isEmpty(user.getPassword())
        || user.getPassword().length() < REQUIRED_PASSWORD_LENGTH) {
      throw new UserCreationException(
          "Cannot create user, password needs to be at least 8 chars long");
    }
    // validate if whitespace exists
    if (StringUtils.containsWhitespace(user.getName())) {
      throw new UserCreationException(
          "Cannot create user, username shouldn't contain white spaces");
    }
    if (StringUtils.containsWhitespace(user.getEmailAddress())) {
      throw new UserCreationException(
          "Cannot create user, email address shouldn't contain white spaces");
    }

    // validate if username exists in Profile/Auth database ignore case
    UserDTO userDTO = userManagementFacade.getUserByNameIgnoreCase(user.getName());
    if (userDTO != null) {
      throw new UserCreationException("Cannot create user, the username has already existed");
    }

    // add entity role to the newly created user
    user.getRoles().add(user.getEntityRole());
    userManagementFacade.publishUserCreationRequestedEvent(user);
    model.addAttribute(ATTRIBUTE_MESSAGE,
        "A request for user " + user.getFirstName() + " " + user.getLastName()
            + " (" + user.getName() + ") has been made. "
            + "It may take a little while before you'll be able to see the new user");
    return "success";
  }

  @PreAuthorize("hasAuthority('heeuser:add:modify')")
  @PostMapping("/updateUser")
  public String updateUser(@ModelAttribute UserDTO user, Model model) {
    userManagementFacade.updateSingleUser(user);
    model.addAttribute(ATTRIBUTE_MESSAGE,
        "The user " + user.getFirstName() + " " + user.getLastName() + " (" + user.getName()
            + ") has been updated");
    return "success";
  }

  @PreAuthorize("hasAuthority('heeuser:add:modify')")
  @PostMapping("/updatePassword")
  public String updatePassword(@ModelAttribute UserPasswordDTO passwordDTO, Model model) {

    if (!StringUtils.equals(passwordDTO.getPassword(), passwordDTO.getConfirmPassword())) {
      throw new PasswordException("Passwords do not match");
    }

    userManagementFacade.updatePassword(passwordDTO);
    model.addAttribute(ATTRIBUTE_MESSAGE, "Password has been updated for the user");
    return "success";
  }

  @PreAuthorize("hasAuthority('heeuser:delete')")
  @PostMapping("/deleteUser")
  public String deleteUser(@ModelAttribute UserDTO user, Model model) {
    userManagementFacade.publishDeleteAuthenticationUserRequestedEvent(user.getName());
    model.addAttribute(ATTRIBUTE_MESSAGE,
        "The user " + user.getName()
            + " has been deleted. This may take a while to show up on the system");
    return "success";
  }
}
