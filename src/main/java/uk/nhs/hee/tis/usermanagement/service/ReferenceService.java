package uk.nhs.hee.tis.usermanagement.service;

import com.transformuk.hee.tis.reference.api.dto.DBCDTO;
import com.transformuk.hee.tis.reference.client.impl.ReferenceServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class ReferenceService {

  @Autowired
  ReferenceServiceImpl remoteReferenceService;

  public Set<DBCDTO> getAllDBCs () {
    return remoteReferenceService.getAllDBCs();
  }
}
