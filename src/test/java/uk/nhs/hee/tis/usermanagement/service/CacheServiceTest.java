package uk.nhs.hee.tis.usermanagement.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

public class CacheServiceTest {

  private CacheService service;
  private CacheManager cacheManager;

  @Before
  public void setUp() {
    cacheManager = mock(CacheManager.class);
    service = new CacheService(cacheManager);
  }

  @Test
  public void clearAllCaches() {
    when(cacheManager.getCacheNames()).thenReturn(Arrays.asList("cache1", "cache2", "cache3"));

    Cache cache1 = mock(Cache.class);
    when(cacheManager.getCache("cache1")).thenReturn(cache1);
    Cache cache2 = mock(Cache.class);
    when(cacheManager.getCache("cache2")).thenReturn(cache2);
    Cache cache3 = mock(Cache.class);
    when(cacheManager.getCache("cache3")).thenReturn(cache3);

    service.clearAllCaches();
    verify(cache1).clear();
    verify(cache2).clear();
    verify(cache3).clear();
  }
}
