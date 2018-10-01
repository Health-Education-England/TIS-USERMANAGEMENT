package uk.nhs.hee.tis.usermanagement.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

//  @RequestMapping(method = RequestMethod.POST, value = "/createUser")
//  public String createUser(@RequestParam("Forename") String forename) {
//    System.out.println(forename);
//    return "success";
//  }

  @GetMapping("/user")
  public String getCompleteUser(@RequestParam String userName, Model model) {
    Optional<UserDTO> completeUserDTO = userManagementFacade.getCompleteUser(userName);
    if (completeUserDTO.isPresent()) {
      model.addAttribute("user", completeUserDTO.get());
    }
    return "userEdit";
  }

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

  @GetMapping("/createUser")
  public String viewCreateUser() {
    return "createUser";
  }


  @PostMapping("/createUser")
  public String createUser() {
    return "createUser";
  }

  @PostMapping("/updateUser")
  public String updateUser(@ModelAttribute UserDTO user, RedirectAttributes attributes) {
    userManagementFacade.updateSingleUser(user);
    attributes.addFlashAttribute("message",
        "The user " + user.getFirstName() + " " + user.getLastName() + " (" + user.getName() + ") has been updated");
    return "redirect:/allUsers";
  }
}
