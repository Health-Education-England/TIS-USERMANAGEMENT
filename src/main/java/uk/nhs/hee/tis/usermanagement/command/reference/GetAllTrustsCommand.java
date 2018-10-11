package uk.nhs.hee.tis.usermanagement.command.reference;

import com.google.common.collect.Lists;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

public class GetAllTrustsCommand extends HystrixCommand<List<TrustDTO>> {

  private static final String COMMAND_KEY = "REFERENCE_COMMANDS";
  private static final Logger LOG = LoggerFactory.getLogger(GetAllTrustsCommand.class);

  private RestTemplate referenceRestTemplate;
  private String serviceUrl;
  private int page;
  private int size;

  public GetAllTrustsCommand(RestTemplate referenceRestTemplate, String serviceUrl, int page, int size) {
    super(HystrixCommandGroupKey.Factory.asKey(COMMAND_KEY));
    this.referenceRestTemplate = referenceRestTemplate;
    this.serviceUrl = serviceUrl;
    this.page = page;
    this.size = size;
  }

  @Override
  protected List<TrustDTO> getFallback() {
    LOG.warn("An error occurred while getting Trusts from the Reference service. Returning empty List");
    LOG.debug("Data that was sent, serviceUrl: [{}] page: [{}], size: [{}]", serviceUrl, page, size);
    return Lists.newArrayList();
  }

  @Override
  protected List<TrustDTO> run() throws Exception {
    try {
      ParameterizedTypeReference<List<TrustDTO>> trustDtoListType = new ParameterizedTypeReference<List<TrustDTO>>() {
      };


      UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serviceUrl + "/api/trusts")
          .queryParam("page", page  )
          .queryParam("size", size);
      ResponseEntity<List<TrustDTO>> result = referenceRestTemplate.exchange(builder.toUriString(), HttpMethod.GET, null, trustDtoListType);

      return result.getBody();
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }
}
