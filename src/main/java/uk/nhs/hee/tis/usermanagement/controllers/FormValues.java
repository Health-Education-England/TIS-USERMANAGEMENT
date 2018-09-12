package uk.nhs.hee.tis.usermanagement.controllers;

import com.transformuk.hee.tis.profile.service.dto.HeeUserDTO;
import com.transformuk.hee.tis.security.model.UserProfile;
import org.bouncycastle.math.raw.Mod;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.transformuk.hee.tis.profile.client.service.impl.ProfileServiceImpl;
import uk.nhs.hee.tis.usermanagement.model.HeeUser;

import java.awt.*;
import java.security.PublicKey;
import java.util.Map;
import java.util.List;


@RestController
@RequestMapping("/api")
public class FormValues {


//  @RequestMapping(method = RequestMethod.POST, value = "/createUser",consumes = MediaType.ALL_VALUE)
  // public String createUser(@RequestParam Map<String, Object> postValues) {
//  public String createUser(Model model) {
//
//    System.out.println("bananas");
//    Map<String, Object> stringObjectMap = model.asMap();
//    stringObjectMap.forEach((k, v) -> System.out.println(k + " " + v));
//    return "Success";
//  }

  // ProfileServiceImpl profileService = new ProfileServiceImpl(RATE_LIMIT,BULK_RATE_LIMIT);
  @Autowired
  private ProfileServiceImpl profileServiceImpl;

  @RequestMapping(method = RequestMethod.POST, value = "/createUser")
  public String createUser(@RequestParam("Forename") String forename) {
    System.out.println(forename);
    return "success";
  }

  @RequestMapping(method = RequestMethod.POST, value = "/getUser")
  public HeeUser getHeeUser(@RequestParam("UserName") String userName) {
    String test = "test";
    //UserProfile userProfile = profileServiceImpl.getSingleUser(userName);

    HeeUser heeUser = new HeeUser();
    //heeUser.setName(userProfile.getUserName());
    return heeUser;
  }
//
////  @RequestMapping(method=RequestMethod.GET, value="/getUser1")
//  @GetMapping("/getUser1")
//  public String getHeeUser1(Model model, @ModelAttribute("userName") String userName) {
//    String test = "test";
//    if (userName != null) {
//      model.addAttribute("user", profileServiceImpl.getSingleUser(userName));
//    }
//    else {
//      // DO SOMETHING HERE
//    }
//    return "userEdit";
//  }

  @GetMapping("/allUsers")
  public ResponseEntity<List<HeeUserDTO>> getAllUsers() {
    //ResponseEntity<List<HeeUserDTO>> users = profileServiceImpl.getAllAdminUsers();

    String test = "test";
    // get json object
    //JSONObject allUsers = profileServiceImpl.getAllAdminUsers();

    // or just get a list of DTOS? This seems simpler...
    List<HeeUserDTO> heeUserDTOS = profileServiceImpl.getAllAdminUsers();
    return ResponseEntity.ok(heeUserDTOS) ;
  }

  //@RequestMapping(method=RequestMethod.POST, value="/updateUser")


}
