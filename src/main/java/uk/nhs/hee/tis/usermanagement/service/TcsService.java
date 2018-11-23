package uk.nhs.hee.tis.usermanagement.service;

import com.transformuk.hee.tis.tcs.api.dto.ProgrammeDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.collections4.CollectionUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.apache.commons.lang3.StringUtils;
import uk.nhs.hee.tis.usermanagement.command.tcs.GetAllProgrammesCommand;

import java.util.ArrayList;
import java.util.List;

@Service
public class TcsService {
  private static final int PAGE_SIZE = 500;

  private static final Logger LOG = LoggerFactory.getLogger(TcsService.class);

  @Autowired
  private RestTemplate tcsRestTemplate;

  @Value("${tcs.service.url}")
  private String serviceUrl;

  private List<ProgrammeDTO> dumbProgrammeCache;

  public List<ProgrammeDTO> getAllProgrammes() {
    if (CollectionUtils.isEmpty(dumbProgrammeCache)) {
      boolean hasNext = true;
      dumbProgrammeCache = new ArrayList<>();
      int page = 0;
      while (hasNext) {

        GetAllProgrammesCommand getAllProgrammesCommand = new GetAllProgrammesCommand(tcsRestTemplate, serviceUrl, page, PAGE_SIZE);
        List<ProgrammeDTO> result = getAllProgrammesCommand.execute();
        dumbProgrammeCache.addAll(result);
        if (CollectionUtils.isNotEmpty(result)) {
          page++;
        } else {
          hasNext = false;
        }
      }

      dumbProgrammeCache.sort((o1, o2) -> StringUtils.compare(o1.getProgrammeName(), o2.getProgrammeName()));
    }
    return dumbProgrammeCache;
  }

  public List<ProgrammeDTO> getDumbProgrammeCache() {
    return dumbProgrammeCache;
  }
}
