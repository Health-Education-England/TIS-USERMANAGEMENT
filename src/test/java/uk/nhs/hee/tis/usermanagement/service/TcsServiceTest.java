package uk.nhs.hee.tis.usermanagement.service;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.transformuk.hee.tis.tcs.api.dto.ProgrammeDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

public class TcsServiceTest {

  private TcsService service;
  private RestTemplate restTemplate;

  @Before
  public void setUp() {
    restTemplate = mock(RestTemplate.class);
    service = new TcsService(restTemplate);
    ReflectionTestUtils.setField(service, "serviceUrl", "http://tcs.com");
  }

  @Test
  public void getAllProgrammesShouldGetAllProgrammesByMakingMultipleRequests() {
    ArrayList<ProgrammeDTO> programmes = Lists.newArrayList();
    for (long i = 0; i < 700; i++) {
      ProgrammeDTO programmeDto = new ProgrammeDTO();
      programmeDto.setId(i);
      programmeDto.setProgrammeName(UUID.randomUUID().toString());
      programmes.add(programmeDto);
    }

    ArgumentCaptor<ParameterizedTypeReference<List<ProgrammeDTO>>> captor = ArgumentCaptor
        .forClass(ParameterizedTypeReference.class);

    when(restTemplate
        .exchange(startsWith("http://tcs.com/api/programmes?page=0&size=500"), eq(HttpMethod.GET),
            eq(null), captor.capture())).thenReturn(ResponseEntity.ok(programmes.subList(0, 500)));
    when(restTemplate
        .exchange(startsWith("http://tcs.com/api/programmes?page=1&size=500"), eq(HttpMethod.GET),
            eq(null), captor.capture()))
        .thenReturn(ResponseEntity.ok(programmes.subList(500, 700)));
    when(restTemplate
        .exchange(startsWith("http://tcs.com/api/programmes?page=2&size=500"), eq(HttpMethod.GET),
            eq(null), captor.capture())).thenReturn(ResponseEntity.ok(Lists.newArrayList()));

    List<ProgrammeDTO> allProgrammes = service.getAllProgrammes();

    Assert.assertEquals(programmes.size(), allProgrammes.size());
  }
}
