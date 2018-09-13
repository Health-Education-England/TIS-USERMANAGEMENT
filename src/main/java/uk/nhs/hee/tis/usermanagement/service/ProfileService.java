package uk.nhs.hee.tis.usermanagement.service;

import com.transformuk.hee.tis.profile.client.service.impl.ProfileServiceImpl;
import com.transformuk.hee.tis.profile.service.dto.HeeUserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfileService {

  @Autowired
  private ProfileServiceImpl profileServiceImpl;

  public List<HeeUserDTO> getAllUsers(Pageable pageable) {
    List<HeeUserDTO> heeUserDTOS = profileServiceImpl.getAllAdminUsers();
    return heeUserDTOS;
  }
}
