package uk.nhs.hee.tis.usermanagement.command.profile;

import com.google.gson.Gson;
import com.transformuk.hee.tis.profile.client.service.impl.ProfileServiceImpl;
import com.transformuk.hee.tis.profile.service.dto.HeeUserDTO;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class CreateUserCommand extends ProfileHystrixCommand<Optional<HeeUserDTO>> {

  private static final String HEE_USERS_ENDPOINT = "/api/hee-users";
  private static final Logger LOG = LoggerFactory.getLogger(CreateUserCommand.class);
  private static final Gson GSON = new Gson();

  private ProfileServiceImpl profileServiceImpl;
  private HeeUserDTO userToCreateDTO;
  private Throwable throwable;

  public CreateUserCommand(ProfileServiceImpl profileServiceImpl, HeeUserDTO userToCreateDTO) {
    this.profileServiceImpl = profileServiceImpl;
    this.userToCreateDTO = userToCreateDTO;
  }

  @Override
  protected Optional<HeeUserDTO> getFallback() {
    LOG.warn("An occurred while attempting to create a hee user in Profile service, returning an empty optional as fallback");
    LOG.debug("Data that was sent: [{}]", GSON.toJson(userToCreateDTO));
    LOG.warn("Exception: [{}]", ExceptionUtils.getStackTrace(throwable));
    return Optional.empty();
  }

  @Override
  protected Optional<HeeUserDTO> run() throws Exception {
    try {
      HeeUserDTO createdUserDto = profileServiceImpl.createDto(userToCreateDTO, HEE_USERS_ENDPOINT, HeeUserDTO.class);
      return Optional.of(createdUserDto);
    } catch (Throwable e) {
      this.throwable = e;
      throw e;
    }
  }
}
