package uk.nhs.hee.tis.usermanagement.service;

import com.transformuk.hee.tis.tcs.api.dto.ProgrammeDTO;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.nhs.hee.tis.usermanagement.command.tcs.GetAllProgrammesCommand;

@Service
public class TcsService {

  private static final Logger LOG = LoggerFactory.getLogger(CacheService.class);

  private static final int PAGE_SIZE = 500;

  private final RestTemplate tcsRestTemplate;

  @Value("${tcs.service.url}")
  private String serviceUrl;

  TcsService(RestTemplate tcsRestTemplate) {
    this.tcsRestTemplate = tcsRestTemplate;
  }

  /**
   * Get all current programmes from the TCS service.
   *
   * @return The found programmes.
   */
  @Cacheable("programmes")
  public List<ProgrammeDTO> getAllProgrammes() {
    LOG.debug("Retrieving all programmes.");
    List<ProgrammeDTO> programmes = new ArrayList<>();
    int page = 0;

    while (true) {
      GetAllProgrammesCommand getAllProgrammesCommand = new GetAllProgrammesCommand(tcsRestTemplate,
          serviceUrl, page, PAGE_SIZE);
      List<ProgrammeDTO> result = getAllProgrammesCommand.execute();

      if (CollectionUtils.isNotEmpty(result)) {
        programmes.addAll(result);
        page++;
      } else {
        break;
      }
    }

    programmes.sort((o1, o2) -> StringUtils.compare(o1.getProgrammeName(), o2.getProgrammeName()));
    LOG.debug("{} programmes found.", programmes.size());
    return programmes;
  }
}
