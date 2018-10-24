package uk.nhs.hee.tis.usermanagement.resource;

import com.transformuk.hee.tis.reference.api.dto.DBCDTO;
import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.util.StringUtils;
import uk.nhs.hee.tis.usermanagement.DTOs.CreateUserDTO;
import uk.nhs.hee.tis.usermanagement.DTOs.UserDTO;
import uk.nhs.hee.tis.usermanagement.DTOs.UserPasswordDTO;
import uk.nhs.hee.tis.usermanagement.exception.PasswordException;
import uk.nhs.hee.tis.usermanagement.exception.UserCreationException;
import uk.nhs.hee.tis.usermanagement.facade.UserManagementFacade;

import java.util.List;


@Controller
public class UserManagementController {

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

    List<String> allRoles = userManagementFacade.getAllRoles();
    List<DBCDTO> allDBCs = userManagementFacade.getAllDBCs();
    List<TrustDTO> allTrusts = userManagementFacade.getAllTrusts();

    model.addAttribute("roles", allRoles);
    model.addAttribute("designatedBodyCodes", allDBCs);
    model.addAttribute("trusts", allTrusts);
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

  @PreAuthorize("hasAuthority('heeuser:add:modify')")
  @GetMapping("/createUser")
  public String viewCreateUser(Model model) {
    model.addAttribute("user", new CreateUserDTO());

    List<String> allRoles = userManagementFacade.getAllRoles();
    List<DBCDTO> allDBCs = userManagementFacade.getAllDBCs();
    List<TrustDTO> allTrusts = userManagementFacade.getAllTrusts();

    model.addAttribute("roles", allRoles);
    model.addAttribute("designatedBodyCodes", allDBCs);
    model.addAttribute("trusts", allTrusts);

    return "createUser";
  }

  @PreAuthorize("hasAuthority('heeuser:add:modify')")
  @PostMapping("/createUser")
  public String createUser(@ModelAttribute CreateUserDTO user, RedirectAttributes attributes) {
    if (!StringUtils.equals(user.getPassword(), user.getConfirmPassword())) {
      throw new UserCreationException("Cannot create user, passwords do not match");
    } else if (StringUtils.isEmpty(user.getPassword()) || user.getPassword().length() < REQUIRED_PASSWORD_LENGTH) {
      throw new UserCreationException("Cannot create user, password needs to be at least 8 chars long");
    }
    userManagementFacade.createUser(user);
    attributes.addFlashAttribute("message",
        "The user " + user.getFirstName() + " " + user.getLastName() + " (" + user.getName() + ") has been created");
    return "redirect:/allUsers";

  }

  @PreAuthorize("hasAuthority('heeuser:add:modify')")
  @PostMapping("/updateUser")
  public String updateUser(@ModelAttribute UserDTO user, RedirectAttributes attributes) {
    userManagementFacade.updateSingleUser(user);
    attributes.addFlashAttribute("message",
        "The user " + user.getFirstName() + " " + user.getLastName() + " (" + user.getName() + ") has been updated");
    return "redirect:/allUsers";
  }

  @PreAuthorize("hasAuthority('heeuser:add:modify')")
  @PostMapping("/updatePassword")
  public String updatePassword(@ModelAttribute UserPasswordDTO passwordDTO, RedirectAttributes attributes) {

    if (!StringUtils.equals(passwordDTO.getPassword(), passwordDTO.getConfirmPassword())) {
      throw new PasswordException("Passwords do not match");
    }

    userManagementFacade.updatePassword(passwordDTO);
    attributes.addFlashAttribute("message",
        "Password has been updated for the user");
    return "redirect:/allUsers";
  }

  @PreAuthorize("hasAuthority('heeuser:delete')")
  @PostMapping("/deleteUser")
  public String deleteUser(@ModelAttribute UserDTO user, RedirectAttributes attributes) {
    userManagementFacade.deleteUser(user.getName());
    attributes.addFlashAttribute("message",
        "The user " + user.getName() + " has been deleted");
    return "redirect:/allUsers";
  }
}
