package uk.nhs.hee.tis.usermanagement.mapper;

import com.amazonaws.services.cognitoidp.model.AdminCreateUserResult;
import com.amazonaws.services.cognitoidp.model.AdminGetUserResult;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.amazonaws.services.cognitoidp.model.AuthEventType;
import com.amazonaws.services.cognitoidp.model.ChallengeResponseType;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import uk.nhs.hee.tis.usermanagement.DTOs.AuthenticationUserDto;
import uk.nhs.hee.tis.usermanagement.DTOs.UserAuthEventDto;

/**
 * A mapper to convert from a Cognito result object.
 */
@Mapper(componentModel = "spring")
public abstract class CognitoResultMapper {

  /**
   * Convert a  Cognito {@link AuthEventType} to a DTO.
   */
  @Mapping(target = "event", source = "eventType")
  @Mapping(target = "result", source = "eventResponse")
  @Mapping(target = "eventDateTime", source = "creationDate")
  @Mapping(target = "device", source = "eventContextData.deviceName")
  @Mapping(target = "challenges", source = "challengeResponses")
  public abstract UserAuthEventDto toUserAuthEventDto(AuthEventType authEventTypes);

  /**
   * Convert AWS provided {@link Date} to {@link LocalDateTime}.
   */
  LocalDateTime creationDateToEventDateTime(Date creationDate) {
    return creationDate.toInstant().atZone(ZoneId.systemDefault())
        .toLocalDateTime();
  }

  /**
   * Convert a list of Cognito {@link ChallengeResponseType} to a comma separated string.
   */
  String challengeResponsesToChallenges(
      List<ChallengeResponseType> challengeResponses) {
    return challengeResponses.stream()
        .map(c -> String.format("%s:%s", c.getChallengeName(), c.getChallengeResponse()))
        .collect(Collectors.joining(", "));
  }

  /**
   * Convert a list of Cognito {@link AuthEventType} to a list of DTOs.
   */
  public abstract List<UserAuthEventDto> toUserAuthEventDtos(List<AuthEventType> authEventTypes);

  /**
   * Convert a cognito admin-create-user result to an authentication user.
   *
   * @param cognitoResult the cognito result to convert.
   * @return The converted authentication user.
   */
  @Mapping(target = "attributes", source = "user.attributes")
  @Mapping(target = "enabled", source = "user.enabled")
  public abstract AuthenticationUserDto toAuthenticationUser(AdminCreateUserResult cognitoResult);

  /**
   * Convert a cognito admin-get-user result to an authentication user.
   *
   * @param cognitoResult the cognito result to convert.
   * @return The converted authentication user.
   */
  @Mapping(target = "attributes", source = "userAttributes")
  public abstract AuthenticationUserDto toAuthenticationUser(AdminGetUserResult cognitoResult);

  /**
   * Convert a list of Cognito {@link AttributeType} to a generic map.
   *
   * @param attributes The Cognito attributes.
   * @return The converted generic attributes.
   */
  Map<String, List<String>> convertAttributes(List<AttributeType> attributes) {
    return attributes.stream()
        .collect(Collectors.toMap(
            AttributeType::getName,
            attr -> Collections.singletonList(attr.getValue())
        ));
  }

  /**
   * Extract the required attributes from the map and set them directly on the DTO.
   *
   * @param authenticationUser The DTO to extract from and set values in.
   */
  @AfterMapping
  void extractAttributes(@MappingTarget AuthenticationUserDto authenticationUser) {
    Map<String, List<String>> attributes = authenticationUser.getAttributes();

    authenticationUser.setId(getAttributeValue(attributes, "sub"));
    authenticationUser.setGivenName(getAttributeValue(attributes, "given_name"));
    authenticationUser.setFamilyName(getAttributeValue(attributes, "family_name"));

    String email = getAttributeValue(attributes, "email");
    authenticationUser.setUsername(email);
    authenticationUser.setEmail(email);
  }

  /**
   * Get the value from the attribute map for the given name.
   *
   * @param attributes The map of attributes.
   * @param name       The name of the attribute to get.
   * @return The attribute value, or null if it does not exist.
   */
  private String getAttributeValue(Map<String, List<String>> attributes, String name) {
    List<String> values = attributes.get(name);

    if (values != null && !values.isEmpty()) {
      return values.get(0);
    }

    return null;
  }
}
