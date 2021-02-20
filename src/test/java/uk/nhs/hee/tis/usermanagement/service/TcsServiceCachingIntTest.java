package uk.nhs.hee.tis.usermanagement.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

import com.transformuk.hee.tis.tcs.api.dto.ProgrammeDTO;
import java.util.Collections;
import java.util.List;
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
public class TcsServiceCachingIntTest {

  private static RestTemplate restTemplate;

  @Autowired
  private CacheService cacheService;

  @Autowired
  private TcsService tcsService;

  @Test
  public void shouldReturnCachedProgrammesWhenAlreadyCached() {
    ProgrammeDTO programme = new ProgrammeDTO();
    programme.setId(40L);
    programme.setProgrammeName("Forty");
    List<ProgrammeDTO> programmes = Collections.singletonList(programme);
    ResponseEntity<List<ProgrammeDTO>> response = ResponseEntity.ok(programmes);

    ParameterizedTypeReference<List<ProgrammeDTO>> responseType =
        new ParameterizedTypeReference<List<ProgrammeDTO>>() {
        };
    when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), eq(responseType)))
        .thenReturn(response, ResponseEntity.ok(Collections.emptyList()));

    List<ProgrammeDTO> allProgrammes1 = tcsService.getAllProgrammes();
    List<ProgrammeDTO> allProgrammes2 = tcsService.getAllProgrammes();

    assertThat(allProgrammes1, is(programmes));
    assertThat(allProgrammes2, is(programmes));

    verify(restTemplate, times(2))
        .exchange(anyString(), eq(HttpMethod.GET), isNull(), eq(responseType));
  }

  @Test
  public void shouldReturnFreshProgrammesWhenCacheDeleted() {
    ProgrammeDTO programme1 = new ProgrammeDTO();
    programme1.setId(40L);
    programme1.setProgrammeName("Forty");
    List<ProgrammeDTO> programmes1 = Collections.singletonList(programme1);
    ResponseEntity<List<ProgrammeDTO>> response1 = ResponseEntity.ok(programmes1);

    ProgrammeDTO programme2 = new ProgrammeDTO();
    programme2.setId(140L);
    programme2.setProgrammeName("OneForty");
    List<ProgrammeDTO> programmes2 = Collections.singletonList(programme2);
    ResponseEntity<List<ProgrammeDTO>> response2 = ResponseEntity.ok(programmes2);

    ParameterizedTypeReference<List<ProgrammeDTO>> responseType =
        new ParameterizedTypeReference<List<ProgrammeDTO>>() {
        };
    when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), eq(responseType)))
        .thenReturn(response1, ResponseEntity.ok(Collections.emptyList()), response2,
            ResponseEntity.ok(Collections.emptyList()));

    List<ProgrammeDTO> allProgrammes1 = tcsService.getAllProgrammes();
    cacheService.clearAllCaches();
    List<ProgrammeDTO> allProgrammes2 = tcsService.getAllProgrammes();

    assertThat(allProgrammes1, is(programmes1));
    assertThat(allProgrammes2, is(programmes2));

    verify(restTemplate, times(4))
        .exchange(anyString(), eq(HttpMethod.GET), isNull(), eq(responseType));
  }

  @EnableCaching
  @TestConfiguration
  static class Configuration {

    @Primary
    @Bean
    RestTemplate mockRestTemplate() {
      restTemplate = mock(RestTemplate.class);
      return restTemplate;
    }
  }
}
