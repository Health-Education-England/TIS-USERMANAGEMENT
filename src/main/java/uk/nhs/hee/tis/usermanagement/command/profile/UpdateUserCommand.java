package uk.nhs.hee.tis.usermanagement.command.profile;

import com.google.gson.Gson;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.transformuk.hee.tis.profile.client.service.impl.ProfileServiceImpl;
import com.transformuk.hee.tis.profile.service.dto.HeeUserDTO;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class UpdateUserCommand extends ProfileHystrixCommand<Optional<HeeUserDTO>> {

  private static final String COMMAND_KEY = "PROFILE_COMMANDS";
  private static final String HEE_USERS_ENDPOINT = "/api/hee-users";
  private static final Logger LOG = LoggerFactory.getLogger(UpdateUserCommand.class);
  private static final Gson GSON = new Gson();

  private ProfileServiceImpl profileServiceImpl;
  private HeeUserDTO userToUpdateDto;
  private Throwable throwable;

  public UpdateUserCommand(ProfileServiceImpl profileServiceImpl, HeeUserDTO userToUpdateDto) {
    this.profileServiceImpl = profileServiceImpl;
    this.userToUpdateDto = userToUpdateDto;
  }

  @Override
  protected Optional<HeeUserDTO> getFallback() {
    LOG.warn("An error occurred while trying to update a user [{}] in profile service, returning empty optional as fallback", userToUpdateDto.getEmailAddress());
    LOG.debug("Data that was sent, userToUpdate: [{}]", GSON.toJson(userToUpdateDto));
    LOG.warn("Exception: [{}]", ExceptionUtils.getStackTrace(throwable));
    return Optional.empty();
  }

  @Override
  protected Optional<HeeUserDTO> run() throws Exception {
    try {
      HeeUserDTO heeUserDTO = profileServiceImpl.updateDto(userToUpdateDto, HEE_USERS_ENDPOINT, HeeUserDTO.class);
      return Optional.of(heeUserDTO);
    } catch (Throwable e) {
      this.throwable = e;
      throw e;
    }
  }
}
