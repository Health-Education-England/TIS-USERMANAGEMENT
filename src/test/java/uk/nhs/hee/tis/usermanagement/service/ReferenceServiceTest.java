package uk.nhs.hee.tis.usermanagement.service;

import com.google.common.collect.Sets;
import com.transformuk.hee.tis.reference.api.dto.DBCDTO;
import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import com.transformuk.hee.tis.reference.client.impl.ReferenceServiceImpl;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReferenceServiceTest {

  @InjectMocks
  private ReferenceService testObj;

  @Mock
  private ReferenceServiceImpl remoteReferenceServiceMock;
  @Mock
  private RestTemplate referenceRestTemplateMock;

  @Captor
  private ArgumentCaptor<ParameterizedTypeReference<List<TrustDTO>>> parameterizedTypeReferenceArgumentCaptor;

  @Test
  public void getAllDBCsShouldReturnAnEmptySetWhenItFails() {
    when(remoteReferenceServiceMock.getAllDBCs()).thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

    Set<DBCDTO> result = testObj.getAllDBCs();

    Assert.assertTrue(result.isEmpty());
  }

  @Test
  public void getAllDBCsShouldReturnCollectionOfAllDbcs() {
    DBCDTO dbcdto = new DBCDTO();
    dbcdto.setId(1L);
    dbcdto.setName("DBC NAME");

    when(remoteReferenceServiceMock.getAllDBCs()).thenReturn(Sets.newHashSet(dbcdto));

    Set<DBCDTO> result = testObj.getAllDBCs();

    Assert.assertEquals(1, result.size());
    Assert.assertEquals(dbcdto, result.iterator().next());
  }

  @Test
  public void getAllTrustsShouldReturnCachedTrustsAndNotMakeARequestToReference() {
    TrustDTO trustDTO = new TrustDTO();
    trustDTO.setId(1L);
    trustDTO.setTrustName("TRUST NAME");

    ReflectionTestUtils.setField(testObj, "dumbTrustCache", Lists.newArrayList(trustDTO));

    List<TrustDTO> result = testObj.getAllTrusts();

    Assert.assertTrue(!result.isEmpty());

    verify(referenceRestTemplateMock, never()).exchange(anyString(), any(HttpMethod.class), any(RequestEntity.class), any(ParameterizedTypeReference.class));
  }

  @Test
  public void getAllTrustsShouldGetAllTrustsByMakingMultipleRequests() {
    ArrayList<TrustDTO> trusts = Lists.newArrayList();
    for (int i = 0; i < 700; i++) {
      TrustDTO trustDTO = new TrustDTO();
      trustDTO.setId((long)i);
      trustDTO.setCode(UUID.randomUUID().toString());
      trusts.add(trustDTO);
    }

    ReflectionTestUtils.setField(testObj, "serviceUrl", "http://reference.com");

    when(referenceRestTemplateMock.exchange(eq("http://reference.com/api/current/trusts?page=0&size=500"), eq(HttpMethod.GET), eq(null), parameterizedTypeReferenceArgumentCaptor.capture())).thenReturn(ResponseEntity.ok(trusts.subList(0, 500)));
    when(referenceRestTemplateMock.exchange(eq("http://reference.com/api/current/trusts?page=1&size=500"), eq(HttpMethod.GET), eq(null), parameterizedTypeReferenceArgumentCaptor.capture())).thenReturn(ResponseEntity.ok(trusts.subList(500, 700)));
    when(referenceRestTemplateMock.exchange(eq("http://reference.com/api/current/trusts?page=2&size=500"), eq(HttpMethod.GET), eq(null), parameterizedTypeReferenceArgumentCaptor.capture())).thenReturn(ResponseEntity.ok(Lists.newArrayList()));

    List<TrustDTO> allTrusts = testObj.getAllTrusts();

    Assert.assertEquals(trusts.size(), allTrusts.size());
  }
}