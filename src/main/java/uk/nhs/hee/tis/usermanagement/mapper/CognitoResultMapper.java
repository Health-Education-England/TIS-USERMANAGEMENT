package uk.nhs.hee.tis.usermanagement.mapper;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthEventType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ChallengeResponseType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserType;
import uk.nhs.hee.tis.usermanagement.DTOs.AuthenticationUserDto;
import uk.nhs.hee.tis.usermanagement.DTOs.UserAuthEventDto;

/**
 * A mapper to convert from a Cognito result object, created as a quick way to upgrade the aws sdk
 * v1 -> v2 This class was modified from the source generated by Mapstruct @Generated{value =
 * "org.mapstruct.ap.MappingProcessor", date = "2025-07-14T08:51:00+0100", comments = "version:
 * 1.5.5.Final, compiler: javac, environment: Java 11.0.19 (Ubuntu)"}
 */
public abstract class CognitoResultMapper {

  /**
   * Convert a  Cognito {@link AuthEventType} to a DTO.
   */
  @Mapping(target = "event", source = "eventType")
  @Mapping(target = "result", source = "eventResponse")
  @Mapping(target = "eventDate", source = "creationDate")
  @Mapping(target = "device", source = "eventContextData.deviceName")
  @Mapping(target = "challenges", source = "challengeResponses")
  public abstract UserAuthEventDto toUserAuthEventDto(AuthEventType authEventTypes);

  /**
   * Convert a list of Cognito {@link ChallengeResponseType} to a comma separated string.
   */
  String challengeResponsesToChallenges(
      List<ChallengeResponseType> challengeResponses) {
    return challengeResponses.stream()
        .map(c -> String.format("%s:%s", c.challengeName(), c.challengeResponse()))
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
  public abstract AuthenticationUserDto toAuthenticationUser(AdminCreateUserResponse cognitoResult);

  /**
   * Convert a UserType to an authentication user.
   *
   * @param user the User Type to convert.
   * @return the converted authentication user.
   */
  public abstract AuthenticationUserDto toAuthenticationUser(UserType user);

  /**
   * Convert a list of Cognito {@link AttributeType} to a generic map.
   *
   * @param attributes The Cognito attributes.
   * @return The converted generic attributes.
   */
  Map<String, List<String>> convertAttributes(List<AttributeType> attributes) {
    return attributes.stream()
        .collect(Collectors.toMap(
            AttributeType::name,
            attr -> Collections.singletonList(attr.value())
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
