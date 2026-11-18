package uk.nhs.hee.tis.usermanagement.resource;

import com.transformuk.hee.tis.reference.api.dto.DBCDTO;
import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeDTO;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import uk.nhs.hee.tis.usermanagement.facade.UserManagementFacade;
import uk.nhs.hee.tis.usermanagement.validation.CreateUserValidator;
import uk.nhs.hee.tis.usermanagement.validation.PasswordValidator;
import uk.nhs.hee.tis.usermanagement.validation.UserManagementUsernameAvailabilityChecker;

@Slf4j
@Controller
public class UserManagementController {

  private static final String ATTRIBUTE_MESSAGE = "message";
  public static final String SUCCESS = "success";

  private final UserManagementFacade userManagementFacade;
  private final String authProvider;
  private final CreateUserValidator createUserValidator;
  private final PasswordValidator passwordValidator;

  public UserManagementController(UserManagementFacade userManagementFacade,
      @Value("${application.authentication-provider}") String authProvider) {
    this(userManagementFacade, authProvider,
        new CreateUserValidator(
            new UserManagementUsernameAvailabilityChecker(userManagementFacade)),
        new PasswordValidator());
  }

  @Autowired
  public UserManagementController(UserManagementFacade userManagementFacade,
      @Value("${application.authentication-provider}") String authProvider,
      CreateUserValidator createUserValidator,
      PasswordValidator passwordValidator) {
    this.userManagementFacade = userManagementFacade;
    this.authProvider = authProvider;
    this.createUserValidator = createUserValidator;
    this.passwordValidator = passwordValidator;
  }

  @PreAuthorize("hasAuthority('heeuser:view')")
  @GetMapping("/user")
  public String getCompleteUser(@RequestParam String userName, Model model) {
    UserDTO completeUserDTO = userManagementFacade.getCompleteUser(userName);

    model.addAttribute("user", completeUserDTO);
    UserPasswordDTO userPasswordDTO = new UserPasswordDTO();
    userPasswordDTO.setKcId(completeUserDTO.getAuthId());
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
  @GetMapping("/userNotification")
  public String viewNotification() {
    return "userNotification";
  }

  /**
   * Show the roles associated with a list of users.
   *
   * @param search - a whitespace separated collection of usernames to search for
   * @param model  - the model to provide to the view
   * @return the key (view name) for the view to send to the user
   */
  @PreAuthorize("hasAuthority('heeuser:view')")
  @GetMapping("/rolesForUsers")
  public String getRolesForUsers(@RequestParam(required = false, defaultValue = "") String search,
      Model model) {
    List<UserDTO> userDtos = Arrays.stream(search.split("\\s"))
        .map(userManagementFacade::getUserByNameIgnoreCase)
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
    if (!"cognito".equalsIgnoreCase(authProvider)) {
      createUserValidator.validateNameAndEmail(user);
      passwordValidator.validatePasswordsMatch(user.getPassword(), user.getConfirmPassword());
      passwordValidator.validatePasswordLength(user.getPassword());
      createUserValidator.validateUsernameAvailable(user.getName());
    }

    // add entity role to the newly created user
    user.getRoles().add(user.getEntityRole());
    userManagementFacade.publishUserCreationRequestedEvent(user);
    model.addAttribute(ATTRIBUTE_MESSAGE,
        "A request for user " + user.getFirstName() + " " + user.getLastName()
            + " (" + user.getName() + ") has been made. "
            + "It may take a little while before you'll be able to see the new user");
    return SUCCESS;
  }

  @PreAuthorize("hasAuthority('heeuser:add:modify')")
  @PostMapping("/updateUser")
  public String updateUser(@ModelAttribute UserDTO user, Model model) {
    userManagementFacade.updateSingleUser(user);
    model.addAttribute(ATTRIBUTE_MESSAGE,
        "The user " + user.getFirstName() + " " + user.getLastName() + " (" + user.getName()
            + ") has been updated");
    return SUCCESS;
  }

  @PreAuthorize("hasAuthority('heeuser:add:modify')")
  @PostMapping("/updatePassword")
  public String updatePassword(@ModelAttribute UserPasswordDTO passwordDTO, Model model) {
    passwordValidator.validatePasswordsMatch(passwordDTO.getPassword(),
        passwordDTO.getConfirmPassword());
    passwordValidator.validatePasswordLength(passwordDTO.getPassword());

    userManagementFacade.updatePassword(passwordDTO);
    model.addAttribute(ATTRIBUTE_MESSAGE, "Password has been updated for the user");
    return SUCCESS;
  }

  @PreAuthorize("hasAuthority('heeuser:delete')")
  @PostMapping("/deleteUser")
  public String deleteUser(@ModelAttribute UserDTO user, Model model) {
    userManagementFacade.publishDeleteAuthenticationUserRequestedEvent(user.getName());
    model.addAttribute(ATTRIBUTE_MESSAGE,
        "The user " + user.getName()
            + " has been deleted. This may take a while to show up on the system");
    return SUCCESS;
  }
}
