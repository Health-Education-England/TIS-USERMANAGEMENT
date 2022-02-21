package uk.nhs.hee.tis.usermanagement.mapper;

import com.amazonaws.services.cognitoidp.model.AdminCreateUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminUpdateUserAttributesRequest;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.nhs.hee.tis.usermanagement.DTOs.AuthenticationUserDto;
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


  @Mapping(target = "userAttributes", source = "authenticationUser")
  public abstract AdminUpdateUserAttributesRequest toUpdateUserRequest(
      AuthenticationUserDto authenticationUser);

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

  /**
   * Extract a list of Cognito attributes from a {@link AuthenticationUserDto}.
   *
   * @param authenticationUser The user to extract attributes from.
   * @return A list of extracted attributes.
   */
  protected List<AttributeType> extractAttributes(AuthenticationUserDto authenticationUser) {
    // Overwrite the existing attributes values before converting them.
    Map<String, List<String>> attributes = new HashMap<>(authenticationUser.getAttributes());
    attributes.put(GIVEN_NAME, Collections.singletonList(authenticationUser.getGivenName()));
    attributes.put(FAMILY_NAME, Collections.singletonList(authenticationUser.getFamilyName()));
    attributes.put(EMAIL, Collections.singletonList(authenticationUser.getEmail()));

    return convertAttributes(attributes);
  }

  /**
   * Convert a generic attribute map to a list of Cognito {@link AttributeType}.
   *
   * @param attributes The generic attributes.
   * @return The converted Cognito attributes.
   */
  protected List<AttributeType> convertAttributes(Map<String, List<String>> attributes) {
    return attributes.entrySet().stream()
        .filter(e -> !Objects.equals(e.getKey(), "sub"))
        .map(e -> new AttributeType().withName(e.getKey()).withValue(e.getValue().get(0)))
        .collect(Collectors.toList());
  }
}
