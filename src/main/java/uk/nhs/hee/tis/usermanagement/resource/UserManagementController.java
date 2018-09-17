package uk.nhs.hee.tis.usermanagement.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import uk.nhs.hee.tis.usermanagement.DTOs.UserDTO;
import uk.nhs.hee.tis.usermanagement.facade.UserManagementFacade;

import java.util.List;
import java.util.Optional;


@Controller
public class UserManagementController {

  @Autowired
  UserManagementFacade userManagementFacade;

  @RequestMapping(method = RequestMethod.POST, value = "/createUser")
  public String createUser(@RequestParam("Forename") String forename) {
    System.out.println(forename);
    return "success";
  }

  @GetMapping("/user/{username}")
  public ResponseEntity<UserDTO> getCompleteUser(@PathVariable("username") String userName) {
    Optional<UserDTO> completeUserDTO = userManagementFacade.getCompleteUser(userName);
    if (completeUserDTO.isPresent()) {
      return ResponseEntity.ok(completeUserDTO.get());
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping("/allUsers")
  public String getAllUsers(Pageable pageable, Model model) {
    List<UserDTO> userDTOS = userManagementFacade.getAllUsers(pageable);
    model.addAttribute("users", userDTOS);
    return "allUsers";
  }
}
