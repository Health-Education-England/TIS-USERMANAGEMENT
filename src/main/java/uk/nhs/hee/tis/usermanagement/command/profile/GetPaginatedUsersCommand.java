package uk.nhs.hee.tis.usermanagement.command.profile;

import com.google.gson.Gson;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.transformuk.hee.tis.profile.client.service.impl.ProfileServiceImpl;
import com.transformuk.hee.tis.profile.service.dto.HeeUserDTO;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;

import java.util.Collections;

public class GetPaginatedUsersCommand extends ProfileHystrixCommand<Page<HeeUserDTO>> {

  private static final Logger LOG = LoggerFactory.getLogger(GetPaginatedUsersCommand.class);
  private static final Gson GSON = new Gson();

  private ProfileServiceImpl profileServiceImpl;
  private Pageable pageable;
  private String username;
  private Throwable throwable;

  public GetPaginatedUsersCommand(ProfileServiceImpl profileServiceImpl, Pageable pageable, @Nullable String username) {
    this.profileServiceImpl = profileServiceImpl;
    this.pageable = pageable;
    this.username = username;
  }

  @Override
  protected Page<HeeUserDTO> getFallback() {
    LOG.warn("An error occurred while getting a page of users from profile service, returning an empty page of users as fallback");
    LOG.debug("Data that was sent, pageable: [{}] username: [{}]", GSON.toJson(pageable), username);
    LOG.warn("Exception: [{}]", ExceptionUtils.getStackTrace(throwable));
    return new PageImpl<>(Collections.EMPTY_LIST);
  }

  @Override
  protected Page<HeeUserDTO> run() throws Exception {
    try {
      return profileServiceImpl.getAllAdminUsers(pageable, username);
    } catch (Throwable e) {
      this.throwable = e;
      throw e;
    }
  }
}
