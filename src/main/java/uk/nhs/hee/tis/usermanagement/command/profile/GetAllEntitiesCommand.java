package uk.nhs.hee.tis.usermanagement.command.profile;

import com.google.common.collect.Lists;
import com.transformuk.hee.tis.profile.dto.OrganisationalEntityDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GetAllEntitiesCommand extends ProfileHystrixCommand<List<String>> {

  private static final String COMMAND_KEY = "PROFILE_COMMANDS";
  private static final Logger LOG = LoggerFactory.getLogger(GetAllEntitiesCommand.class);

  private RestTemplate profileRestTemplate;
  private String serviceUrl;
  private Throwable throwable;

  public GetAllEntitiesCommand(RestTemplate profileRestTemplate, String serviceUrl) {
    this.profileRestTemplate = profileRestTemplate;
    this.serviceUrl = serviceUrl;
  }

  @Override
  protected List<String> getFallback() {
    LOG.warn("An occurred while getting all Roles in the Profile service, returning an empty List as fallback");
    LOG.debug("Data that was sent: serviceUrl: [{}]", serviceUrl);
    LOG.warn("Exception: [{}]", ExceptionUtils.getStackTrace(throwable));
    return Lists.newArrayList();
  }

  @Override
  protected List<String> run() throws Exception {
    try {
      ParameterizedTypeReference<List<OrganisationalEntityDTO>> entityDtoListType = new ParameterizedTypeReference<List<OrganisationalEntityDTO>>() {
      };

      UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serviceUrl + "/api/entities")
          .queryParam("page", 0)
          .queryParam("size", 500); //quick hack for now as we dont have nowhere near 500 roles

      ResponseEntity<List<OrganisationalEntityDTO>> result = profileRestTemplate.exchange(builder.toUriString(), HttpMethod.GET, null, entityDtoListType);
      List<String> entities = Collections.EMPTY_LIST;
      if (CollectionUtils.isNotEmpty(result.getBody())) {
        entities = result.getBody().stream().map(OrganisationalEntityDTO::getName).sorted().collect(Collectors.toList());
      }
      return entities;
    } catch (Throwable e) {
      this.throwable = e;
      throw e;
    }
  }
}
