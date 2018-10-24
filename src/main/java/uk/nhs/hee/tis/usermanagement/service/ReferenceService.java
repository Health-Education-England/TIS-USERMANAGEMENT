package uk.nhs.hee.tis.usermanagement.service;

import com.transformuk.hee.tis.reference.api.dto.DBCDTO;
import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import com.transformuk.hee.tis.reference.client.impl.ReferenceServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.nhs.hee.tis.usermanagement.command.reference.GetAllDesignatedBodyCodesCommand;
import uk.nhs.hee.tis.usermanagement.command.reference.GetAllTrustsCommand;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class ReferenceService {

  private static final int PAGE_SIZE = 500;

  @Autowired
  private ReferenceServiceImpl remoteReferenceService;
  @Autowired
  private RestTemplate referenceRestTemplate;
  @Value("${reference.service.url}")
  private String serviceUrl;

  private List<TrustDTO> dumbTrustCache;

  public Set<DBCDTO> getAllDBCs() {
    GetAllDesignatedBodyCodesCommand getAllDesignatedBodyCodesCommand = new GetAllDesignatedBodyCodesCommand(remoteReferenceService);
    return getAllDesignatedBodyCodesCommand.execute();
  }

  public List<TrustDTO> getAllTrusts() {
    if (CollectionUtils.isEmpty(dumbTrustCache)) {
      boolean hasNext = true;
      dumbTrustCache = new ArrayList<>();
      int page = 0;
      while (hasNext) {

        GetAllTrustsCommand getAllTrustsCommand = new GetAllTrustsCommand(referenceRestTemplate, serviceUrl, page, PAGE_SIZE);
        List<TrustDTO> result = getAllTrustsCommand.execute();
        dumbTrustCache.addAll(result);
        if (CollectionUtils.isNotEmpty(result)) {
          page++;
        } else {
          hasNext = false;
        }
      }

      dumbTrustCache.sort((o1, o2) -> StringUtils.compare(o1.getCode(), o2.getCode()));
    }
    return dumbTrustCache;
  }

  public List<TrustDTO> getDumbTrustCache() {
    return dumbTrustCache;
  }
}
