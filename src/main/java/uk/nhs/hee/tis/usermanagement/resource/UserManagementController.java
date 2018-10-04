package uk.nhs.hee.tis.usermanagement.resource;

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
import uk.nhs.hee.tis.usermanagement.DTOs.UserDTO;
import uk.nhs.hee.tis.usermanagement.facade.UserManagementFacade;

import java.util.Optional;


@Controller
public class UserManagementController {

  @Autowired
  UserManagementFacade userManagementFacade;

  @PreAuthorize("hasAuthority('heeuser:view')")
  @GetMapping("/user")
  public String getCompleteUser(@RequestParam String userName, Model model) {
    Optional<UserDTO> completeUserDTO = userManagementFacade.getCompleteUser(userName);
    if (completeUserDTO.isPresent()) {
      model.addAttribute("user", completeUserDTO.get());
    }
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
  public String viewCreateUser() {
    return "createUser";
  }

  @PreAuthorize("hasAuthority('heeuser:add:modify')")
  @PostMapping("/createUser")
  public String createUser() {
    return "createUser";
  }

  @PreAuthorize("hasAuthority('heeuser:add:modify')")
  @PostMapping("/updateUser")
  public String updateUser(@ModelAttribute UserDTO user, RedirectAttributes attributes) {
    userManagementFacade.updateSingleUser(user);
    attributes.addFlashAttribute("message",
        "The user " + user.getFirstName() + " " + user.getLastName() + " (" + user.getName() + ") has been updated");
    return "redirect:/allUsers";
  }

  @PreAuthorize("hasAuthority('heeuser:delete')")
  @PostMapping("/deleteUser")
  public String deleteUser(@ModelAttribute UserDTO user, RedirectAttributes attributes) {
    attributes.addFlashAttribute("message",
        "The user " + user.getFirstName() + " " + user.getLastName() + " (" + user.getName() + ") has been deleted");
    return "redirect:/allUsers";
  }
}
