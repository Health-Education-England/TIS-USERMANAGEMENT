package uk.nhs.hee.tis.usermanagement.service;

import com.transformuk.hee.tis.reference.api.dto.DBCDTO;
import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import com.transformuk.hee.tis.reference.client.impl.ReferenceServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class ReferenceService {

  @Autowired
  private ReferenceServiceImpl remoteReferenceService;
  @Autowired
  private RestTemplate referenceRestTemplate;
  @Value("${reference.service.url}")
  private String serviceUrl;

  private List<TrustDTO> dumbTrustCache;

  public Set<DBCDTO> getAllDBCs() {
    return remoteReferenceService.getAllDBCs();
  }

  public List<TrustDTO> getAllTrusts() {
    if (CollectionUtils.isEmpty(dumbTrustCache)) {
      ParameterizedTypeReference<List<TrustDTO>> trustDtoListType = new ParameterizedTypeReference<List<TrustDTO>>() {
      };

      boolean hasNext = true;
      dumbTrustCache = new ArrayList<>();
      int page = 0;
      int pageSize = 500;
      while (hasNext) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serviceUrl + "/api/trusts")
            .queryParam("page", page)
            .queryParam("size", pageSize);

        ResponseEntity<List<TrustDTO>> result = referenceRestTemplate.exchange(builder.toUriString(), HttpMethod.GET, null, trustDtoListType);
        dumbTrustCache.addAll(result.getBody());
        List<String> link = result.getHeaders().get("Link");
        if (CollectionUtils.isNotEmpty(link) && link.get(0).contains("next")) {
          page++;
          pageSize += 500;
        } else {
          hasNext = false;
        }
      }

      dumbTrustCache.sort((o1, o2) -> StringUtils.compare(o1.getCode(), o2.getCode()));
    }
    return dumbTrustCache;
  }
}
