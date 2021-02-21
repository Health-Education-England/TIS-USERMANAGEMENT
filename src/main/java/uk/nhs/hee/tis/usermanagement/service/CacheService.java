package uk.nhs.hee.tis.usermanagement.service;

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CacheService {

  private static final Logger LOG = LoggerFactory.getLogger(CacheService.class);

  private final CacheManager cacheManager;

  CacheService(CacheManager cacheManager) {
    this.cacheManager = cacheManager;
  }

  /**
   * Clear all caches periodically so new or changed data can be brought in.
   */
  @Scheduled(cron = "${application.cache-evict-schedule}")
  public void clearAllCaches() {
    for (String name : cacheManager.getCacheNames()) {
      LOG.debug("Clearing cache '{}'", name);
      Objects.requireNonNull(cacheManager.getCache(name)).clear();
    }
  }
}
