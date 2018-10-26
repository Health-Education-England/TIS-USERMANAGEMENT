package uk.nhs.hee.tis.usermanagement.command.reference;

import com.google.common.collect.Sets;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.transformuk.hee.tis.reference.api.dto.DBCDTO;
import com.transformuk.hee.tis.reference.client.impl.ReferenceServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class GetAllDesignatedBodyCodesCommand extends ReferenceHystrixCommand<Set<DBCDTO>> {

  private static final Logger LOG = LoggerFactory.getLogger(GetAllDesignatedBodyCodesCommand.class);

  private ReferenceServiceImpl remoteReferenceService;
  private Throwable throwable;

  public GetAllDesignatedBodyCodesCommand(ReferenceServiceImpl remoteReferenceService) {
    this.remoteReferenceService = remoteReferenceService;
  }

  @Override
  protected Set<DBCDTO> getFallback() {
    LOG.warn("An error occurred when getting all DBC's from the reference service, returning empty Set");
    return Sets.newHashSet();
  }

  @Override
  protected Set<DBCDTO> run() throws Exception {
    try {
      return remoteReferenceService.getAllDBCs();
    } catch (Throwable e) {
      this.throwable = e;
      throw e;
    }
  }
}
