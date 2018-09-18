package uk.nhs.hee.tis.usermanagement.service;

import com.transformuk.hee.tis.profile.client.service.impl.ProfileServiceImpl;
import com.transformuk.hee.tis.profile.service.dto.HeeUserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfileService {

  @Autowired
  private ProfileServiceImpl profileServiceImpl;

  public Page<HeeUserDTO> getAllUsers(Pageable pageable, String search) {
    Page<HeeUserDTO> heeUserDTOS = profileServiceImpl.getAllAdminUsers(pageable,search);
    return heeUserDTOS;
  }

  public HeeUserDTO getUserByUsername(String username) {
    HeeUserDTO heeUserDTO = profileServiceImpl.getSingleAdminUser(username);
    return heeUserDTO;
  }
}
