package uk.nhs.hee.tis.usermanagement.command.reference;

import com.google.common.collect.Lists;
import com.transformuk.hee.tis.reference.api.dto.validation.OrganisationalEntityDTO;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

public class GetAllEntitiesCommand extends ReferenceHystrixCommand<List<OrganisationalEntityDTO>> {

  private static final Logger LOG = LoggerFactory.getLogger(GetAllEntitiesCommand.class);

  private RestTemplate referenceRestTemplate;
  private String serviceUrl;
  private Throwable throwable;

  public GetAllEntitiesCommand(RestTemplate referenceRestTemplate, String serviceUrl) {
    this.referenceRestTemplate = referenceRestTemplate;
    this.serviceUrl = serviceUrl;
  }

  @Override
  protected List<OrganisationalEntityDTO> getFallback() {
    LOG.warn("An occurred while getting all OrganisationalEntities in the Reference service, returning an empty List as fallback");
    LOG.debug("Data that was sent: serviceUrl: [{}]", serviceUrl);
    LOG.warn("Exception: [{}]", ExceptionUtils.getStackTrace(throwable));
    return Lists.newArrayList();
  }

  @Override
  protected List<OrganisationalEntityDTO> run() throws Exception {
    try {
      ParameterizedTypeReference<List<OrganisationalEntityDTO>> entityDtoListType = new ParameterizedTypeReference<List<OrganisationalEntityDTO>>() {
      };

      UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serviceUrl + "/api/organisational-entities")
          .queryParam("page", 0)
          .queryParam("size", 100);

      ResponseEntity<List<OrganisationalEntityDTO>> result = referenceRestTemplate.exchange(builder.toUriString(), HttpMethod.GET, null, entityDtoListType);

      return result.getBody();
    } catch (Throwable e) {
      this.throwable = e;
      throw e;
    }
  }
}
