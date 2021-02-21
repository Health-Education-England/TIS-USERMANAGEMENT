package uk.nhs.hee.tis.usermanagement.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

import com.transformuk.hee.tis.reference.api.dto.DBCDTO;
import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import com.transformuk.hee.tis.reference.client.impl.ReferenceServiceImpl;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {"application.cache-evict-cron=0 0 0 1 1 * 1970"})
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
public class ReferenceServiceCachingIntTest {

  private static ReferenceServiceImpl referenceServiceImpl;
  private static RestTemplate restTemplate;

  @Autowired
  private CacheService cacheService;

  @Autowired
  private ReferenceService referenceService;

  @Test
  public void shouldReturnCachedDbcsWhenAlreadyCached() {
    Set<DBCDTO> dbcs = Collections.singleton(new DBCDTO());
    when(referenceServiceImpl.getAllDBCs()).thenReturn(dbcs);

    Set<DBCDTO> allDbcs1 = referenceService.getAllDBCs();
    Set<DBCDTO> allDbcs2 = referenceService.getAllDBCs();

    assertThat(allDbcs1, sameInstance(dbcs));
    assertThat(allDbcs2, sameInstance(dbcs));

    verify(referenceServiceImpl).getAllDBCs();
    verifyNoMoreInteractions(referenceServiceImpl);
  }

  @Test
  public void shouldReturnFreshDbcsWhenCacheDeleted() {
    Set<DBCDTO> dbcs1 = Collections.singleton(new DBCDTO());
    Set<DBCDTO> dbcs2 = Collections.singleton(new DBCDTO());
    when(referenceServiceImpl.getAllDBCs()).thenReturn(dbcs1, dbcs2);

    Set<DBCDTO> allDbcs1 = referenceService.getAllDBCs();
    cacheService.clearAllCaches();
    Set<DBCDTO> allDbcs2 = referenceService.getAllDBCs();

    assertThat(allDbcs1, sameInstance(dbcs1));
    assertThat(allDbcs2, sameInstance(dbcs2));

    verify(referenceServiceImpl, times(2)).getAllDBCs();
    verifyNoMoreInteractions(referenceServiceImpl);
  }

  @Test
  public void shouldReturnCachedTrustsWhenAlreadyCached() {
    TrustDTO trust = new TrustDTO();
    trust.setId(40L);
    trust.setCode("Forty");
    List<TrustDTO> trusts = Collections.singletonList(trust);
    ResponseEntity<List<TrustDTO>> response = ResponseEntity.ok(trusts);

    ParameterizedTypeReference<List<TrustDTO>> responseType =
        new ParameterizedTypeReference<List<TrustDTO>>() {
        };
    when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), eq(responseType)))
        .thenReturn(response, ResponseEntity.ok(Collections.emptyList()));

    List<TrustDTO> allTrusts1 = referenceService.getAllTrusts();
    List<TrustDTO> allTrusts2 = referenceService.getAllTrusts();

    assertThat(allTrusts1, is(trusts));
    assertThat(allTrusts2, is(trusts));

    verify(restTemplate, times(2))
        .exchange(anyString(), eq(HttpMethod.GET), isNull(), eq(responseType));
  }

  @Test
  public void shouldReturnFreshTrustsWhenCacheDeleted() {
    TrustDTO trust1 = new TrustDTO();
    trust1.setId(40L);
    trust1.setCode("Forty");
    List<TrustDTO> trusts1 = Collections.singletonList(trust1);
    ResponseEntity<List<TrustDTO>> response1 = ResponseEntity.ok(trusts1);

    TrustDTO trust2 = new TrustDTO();
    trust2.setId(140L);
    trust2.setCode("OneForty");
    List<TrustDTO> trusts2 = Collections.singletonList(trust2);
    ResponseEntity<List<TrustDTO>> response2 = ResponseEntity.ok(trusts2);

    ParameterizedTypeReference<List<TrustDTO>> responseType =
        new ParameterizedTypeReference<List<TrustDTO>>() {
        };
    when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), eq(responseType)))
        .thenReturn(response1, ResponseEntity.ok(Collections.emptyList()), response2,
            ResponseEntity.ok(Collections.emptyList()));

    List<TrustDTO> allTrusts1 = referenceService.getAllTrusts();
    cacheService.clearAllCaches();
    List<TrustDTO> allTrusts2 = referenceService.getAllTrusts();

    assertThat(allTrusts1, is(trusts1));
    assertThat(allTrusts2, is(trusts2));

    verify(restTemplate, times(4))
        .exchange(anyString(), eq(HttpMethod.GET), isNull(), eq(responseType));
  }

  @EnableCaching
  @TestConfiguration
  static class Configuration {

    @Primary
    @Bean
    ReferenceServiceImpl mockReferenceServiceImpl() {
      referenceServiceImpl = mock(ReferenceServiceImpl.class);
      return referenceServiceImpl;
    }

    @Primary
    @Bean
    RestTemplate mockRestTemplate() {
      restTemplate = mock(RestTemplate.class);
      return restTemplate;
    }
  }
}
