package uk.nhs.hee.tis.usermanagement.resource;

import com.google.common.collect.Lists;
import com.transformuk.hee.tis.profile.service.dto.HeeUserDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import uk.nhs.hee.tis.usermanagement.DTOs.UserDTO;
import uk.nhs.hee.tis.usermanagement.facade.UserManagementFacade;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api")
public class UserManagementResource {

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
  public ResponseEntity<List<UserDTO>> getAllUsers(Pageable pageable) {
    List<UserDTO> userDTOS = userManagementFacade.getAllUsers(pageable);
    return ResponseEntity.ok(userDTOS) ;
  }
}
