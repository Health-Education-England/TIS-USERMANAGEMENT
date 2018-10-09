package uk.nhs.hee.tis.usermanagement.command.profile;

import com.google.gson.Gson;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.transformuk.hee.tis.profile.client.service.impl.ProfileServiceImpl;
import com.transformuk.hee.tis.profile.service.dto.HeeUserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class UpdateUserCommand extends HystrixCommand<Optional<HeeUserDTO>> {

  private static final String COMMAND_KEY = "PROFILE_COMMANDS";
  private static final String HEE_USERS_ENDPOINT = "/api/hee-users";
  private static final Logger LOG = LoggerFactory.getLogger(CreateUserCommand.class);
  private static final Gson GSON = new Gson();

  private ProfileServiceImpl profileServiceImpl;
  private HeeUserDTO userToUpdateDto;

  public UpdateUserCommand(ProfileServiceImpl profileServiceImpl, HeeUserDTO userToUpdateDto)
  {
    super(HystrixCommandGroupKey.Factory.asKey(COMMAND_KEY));
    this.profileServiceImpl = profileServiceImpl;
    this.userToUpdateDto = userToUpdateDto;
  }

  @Override
  protected Optional<HeeUserDTO> getFallback() {
    LOG.warn("An error occurred while trying to update a user in profile service, returning empty optional as fallback");
    LOG.debug("Data that was sent, userToUpdate: [{}]", GSON.toJson(userToUpdateDto));
    return Optional.empty();
  }

  @Override
  protected Optional<HeeUserDTO> run() throws Exception {
    HeeUserDTO heeUserDTO = profileServiceImpl.updateDto(userToUpdateDto, HEE_USERS_ENDPOINT, HeeUserDTO.class);
    return Optional.of(heeUserDTO);
  }
}
