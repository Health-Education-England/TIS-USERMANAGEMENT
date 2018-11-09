package uk.nhs.hee.tis.usermanagement.command.tcs;

import com.google.common.collect.Lists;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

public class GetAllProgrammesCommand extends TcsHystrixCommand<List<ProgrammeDTO>> {

  private static final Logger LOG = LoggerFactory.getLogger(GetAllProgrammesCommand.class);

  private RestTemplate tcsRestTemplate;
  private String serviceUrl;
  private int page;
  private int size;
  private Throwable throwable;

  public GetAllProgrammesCommand(RestTemplate tcsRestTemplate, String serviceUrl, int page, int size) {
    this.tcsRestTemplate = tcsRestTemplate;
    this.serviceUrl = serviceUrl;
    this.page = page;
    this.size = size;
  }

  @Override
  protected List<ProgrammeDTO> getFallback() {
    LOG.warn("An error occurred while getting Programmes from the TCS service. Returning empty List");
    LOG.debug("Data that was sent, serviceUrl: [{}] page: [{}], size: [{}]", serviceUrl, page, size);
    return Lists.newArrayList();
  }

  @Override
  protected List<ProgrammeDTO> run() throws Exception {
    try {
      ParameterizedTypeReference<List<ProgrammeDTO>> trustDtoListType = new ParameterizedTypeReference<List<ProgrammeDTO>>() {
      };


      UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serviceUrl + "/api/programmes")
          .queryParam("page", page)
          .queryParam("size", size);
      ResponseEntity<List<ProgrammeDTO>> result = tcsRestTemplate.exchange(builder.toUriString(), HttpMethod.GET, null, trustDtoListType);

      return result.getBody();
    } catch (Throwable e) {
      this.throwable = e;
      throw e;
    }
  }
}
