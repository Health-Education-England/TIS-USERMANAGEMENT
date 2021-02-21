package uk.nhs.hee.tis.usermanagement.service;

import com.transformuk.hee.tis.reference.api.dto.DBCDTO;
import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import com.transformuk.hee.tis.reference.client.impl.ReferenceServiceImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.nhs.hee.tis.usermanagement.command.reference.GetAllDesignatedBodyCodesCommand;
import uk.nhs.hee.tis.usermanagement.command.reference.GetAllTrustsCommand;

@Service
public class ReferenceService {

  private static final Logger LOG = LoggerFactory.getLogger(ReferenceService.class);

  private static final int PAGE_SIZE = 500;

  private final ReferenceServiceImpl remoteReferenceService;

  private final RestTemplate referenceRestTemplate;

  @Value("${reference.service.url}")
  private String serviceUrl;

  ReferenceService(ReferenceServiceImpl remoteReferenceService,
      RestTemplate referenceRestTemplate) {
    this.remoteReferenceService = remoteReferenceService;
    this.referenceRestTemplate = referenceRestTemplate;
  }

  /**
   * Get all DBCs from the reference service.
   *
   * @return The found DBCs.
   */
  @Cacheable("dbcs")
  public Set<DBCDTO> getAllDBCs() {
    LOG.debug("Retrieving all DBCs.");
    GetAllDesignatedBodyCodesCommand getAllDesignatedBodyCodesCommand =
        new GetAllDesignatedBodyCodesCommand(remoteReferenceService);
    return getAllDesignatedBodyCodesCommand.execute();
  }

  /**
   * This method will bring all the current trusts, although the name is getAllTrusts. It will not
   * pull any inactive trusts.
   *
   * @return The found trusts.
   */
  @Cacheable("trusts")
  public List<TrustDTO> getAllTrusts() {
    LOG.debug("Retrieving all trusts.");
    List<TrustDTO> trusts = new ArrayList<>();
    int page = 0;

    while (true) {
      GetAllTrustsCommand getAllTrustsCommand = new GetAllTrustsCommand(referenceRestTemplate,
          serviceUrl, page, PAGE_SIZE);
      List<TrustDTO> result = getAllTrustsCommand.execute();

      result = result.stream()
          .filter(Objects::nonNull)
          .filter(t -> t.getId() != null)
          .filter(t -> t.getCode() != null)
          .filter(t -> !StringUtils.equals(t.getCode(), "null"))
          .collect(Collectors.toList());

      if (CollectionUtils.isNotEmpty(result)) {
        trusts.addAll(result);
        page++;
      } else {
        break;
      }
    }

    trusts.sort((o1, o2) -> StringUtils.compare(o1.getCode(), o2.getCode()));
    LOG.debug("{} valid trusts found.", trusts.size());
    return trusts;
  }
}
