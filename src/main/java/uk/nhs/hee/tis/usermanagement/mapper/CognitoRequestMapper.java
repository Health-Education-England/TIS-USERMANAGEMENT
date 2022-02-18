package uk.nhs.hee.tis.usermanagement.mapper;

import com.amazonaws.services.cognitoidp.model.AdminCreateUserRequest;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import java.util.ArrayList;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.nhs.hee.tis.usermanagement.DTOs.CreateUserDTO;

/**
 * A mapper for converting to Cognito requests.
 */
@Mapper(componentModel = "spring")
public abstract class CognitoRequestMapper {

  private static final String GIVEN_NAME = "given_name";
  private static final String FAMILY_NAME = "family_name";
  private static final String EMAIL = "email";

  /**
   * Map a {@link CreateUserDTO} to a Cognito {@link AdminCreateUserRequest}.
   *
   * @param createUserDto The DTO to map from.
   * @return The mapped request.
   */
  @Mapping(target = "username", source = "name")
  @Mapping(target = "userAttributes", source = "createUserDto")
  @Mapping(target = "temporaryPassword", source = "password")
  public abstract AdminCreateUserRequest toCreateUserRequest(CreateUserDTO createUserDto);

  /**
   * Extract a list of Cognito attributes from a {@link CreateUserDTO}.
   *
   * @param createUserDto The DTO to extract attributes from.
   * @return A list of extracted attributes.
   */
  protected List<AttributeType> extractAttributes(CreateUserDTO createUserDto) {
    List<AttributeType> attributes = new ArrayList<>();
    attributes.add(
        new AttributeType()
            .withName(GIVEN_NAME)
            .withValue(createUserDto.getFirstName()));
    attributes.add(
        new AttributeType()
            .withName(FAMILY_NAME)
            .withValue(createUserDto.getLastName()));
    attributes.add(
        new AttributeType()
            .withName(EMAIL)
            .withValue(createUserDto.getEmailAddress()));

    return attributes;
  }
}
