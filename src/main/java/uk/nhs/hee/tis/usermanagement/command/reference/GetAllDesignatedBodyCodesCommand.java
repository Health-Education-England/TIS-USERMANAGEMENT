package uk.nhs.hee.tis.usermanagement.command.reference;

import com.google.common.collect.Sets;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.transformuk.hee.tis.reference.api.dto.DBCDTO;
import com.transformuk.hee.tis.reference.client.impl.ReferenceServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class GetAllDesignatedBodyCodesCommand extends HystrixCommand<Set<DBCDTO>> {

  private static final Logger LOG = LoggerFactory.getLogger(GetAllDesignatedBodyCodesCommand.class);
  private static final String COMMAND_KEY = "REFERENCE_COMMANDS";

  private ReferenceServiceImpl remoteReferenceService;

  public GetAllDesignatedBodyCodesCommand(ReferenceServiceImpl remoteReferenceService) {
    super(HystrixCommandGroupKey.Factory.asKey(COMMAND_KEY));
    this.remoteReferenceService = remoteReferenceService;
  }

  @Override
  protected Set<DBCDTO> getFallback() {
    LOG.warn("An error occurred when getting all DBC's from the reference service, returning empty Set");
    return Sets.newHashSet();
  }

  @Override
  protected Set<DBCDTO> run() throws Exception {
    return remoteReferenceService.getAllDBCs();
  }
}
