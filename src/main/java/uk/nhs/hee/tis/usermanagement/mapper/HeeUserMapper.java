package uk.nhs.hee.tis.usermanagement.mapper;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.transformuk.hee.tis.profile.dto.RoleDTO;
import com.transformuk.hee.tis.profile.service.dto.HeeUserDTO;
import com.transformuk.hee.tis.profile.service.dto.UserProgrammeDTO;
import com.transformuk.hee.tis.profile.service.dto.UserTrustDTO;
import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeDTO;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import uk.nhs.hee.tis.usermanagement.DTOs.AuthenticationUserDto;
import uk.nhs.hee.tis.usermanagement.DTOs.CreateUserDTO;
import uk.nhs.hee.tis.usermanagement.DTOs.UserDTO;

@Component
public class HeeUserMapper {

  public List<UserDTO> convertAll(List<HeeUserDTO> heeUserDtos) {
    return heeUserDtos.stream().map(this::convert).collect(Collectors.toList());
  }

  /**
   * Used for the list page where only the profile service info is needed
   *
   * @param heeUserDto The Authorization (profile) information to map
   * @return The UserManagement composite DTO, which will be partially populated
   */
  public UserDTO convert(HeeUserDTO heeUserDto) {
    UserDTO userDto = new UserDTO();
    return mapHeeUserAttributes(userDto, heeUserDto);
  }

  public HeeUserDTO convert(UserDTO userDto, List<TrustDTO> knownTrusts,
      List<ProgrammeDTO> knownProgrammes) {
    Preconditions.checkNotNull(userDto, "stop being stooopid");
    return mapUserAttributes(userDto.getName(), userDto.getFirstName(), userDto.getLastName(),
        userDto.getGmcId(),
        userDto.getPhoneNumber(), userDto.getEmailAddress(), userDto.getActive(),
        userDto.getLocalOffices(),
        userDto.getRoles(), userDto.getAssociatedTrusts(), userDto.getAssociatedProgrammes(),
        knownTrusts, knownProgrammes);
  }

  public HeeUserDTO convert(CreateUserDTO createUserDto, List<TrustDTO> knownTrusts,
      List<ProgrammeDTO> knownProgrammes) {
    Preconditions.checkNotNull(createUserDto, "stop being stooopid");
    return mapUserAttributes(createUserDto.getName(), createUserDto.getFirstName(),
        createUserDto.getLastName(), createUserDto.getGmcId(), createUserDto.getPhoneNumber(),
        createUserDto.getEmailAddress(), createUserDto.isActive(), createUserDto.getLocalOffices(),
        createUserDto.getRoles(), createUserDto.getAssociatedTrusts(),
        createUserDto.getAssociatedProgrammes(), knownTrusts, knownProgrammes);

  }

  private HeeUserDTO mapUserAttributes(String name, String firstName, String lastName, String gmcId,
      String phoneNumber,
      String emailAddress, boolean active, Set<String> localOffices, Set<String> roles,
      Set<String> associatedTrusts,
      Set<String> associatedProgrammes, List<TrustDTO> knownTrusts,
      List<ProgrammeDTO> knownProgrammes) {
    HeeUserDTO heeUserDto = new HeeUserDTO();
    heeUserDto.setName(name);
    heeUserDto.setFirstName(firstName);
    heeUserDto.setLastName(lastName);
    heeUserDto.setGmcId(gmcId);
    heeUserDto.setPhoneNumber(phoneNumber);
    heeUserDto.setEmailAddress(emailAddress);
    heeUserDto.setActive(active);
    heeUserDto.setDesignatedBodyCodes(localOffices);

    if (CollectionUtils.isNotEmpty(roles)) {
      Set<RoleDTO> setOfRoles = roles.stream().map(roleName -> {
        RoleDTO roleDto = new RoleDTO();
        roleDto.setName(roleName);
        return roleDto;
      }).collect(Collectors.toSet());
      heeUserDto.setRoles(setOfRoles);
    }
    heeUserDto.setAssociatedTrusts(mapUserTrust(associatedTrusts, knownTrusts));
    heeUserDto.setAssociatedProgrammes(mapUserProgramme(associatedProgrammes, knownProgrammes));

    return heeUserDto;
  }

  private Set<UserTrustDTO> mapUserTrust(Set<String> associatedTrusts, List<TrustDTO> knownTrusts) {
    Set<UserTrustDTO> trusts = Collections.emptySet();
    if (CollectionUtils.isNotEmpty(associatedTrusts)) {
      Map<Long, TrustDTO> idsToTrust = knownTrusts.stream()
          .collect(Collectors.toMap((TrustDTO::getId), trust -> trust));

      trusts = associatedTrusts.stream().map(Long::parseLong).map(idsToTrust::get).map(trustDto -> {
        UserTrustDTO userTrustDto = new UserTrustDTO();
        userTrustDto.setTrustId(trustDto.getId());
        userTrustDto.setTrustCode(trustDto.getCode());
        userTrustDto.setTrustName(trustDto.getTrustName());
        return userTrustDto;
      }).collect(Collectors.toSet());
    }
    return trusts;
  }

  private Set<UserProgrammeDTO> mapUserProgramme(Set<String> associatedProgrammes,
      List<ProgrammeDTO> knownProgrammes) {
    Set<UserProgrammeDTO> programmes = Collections.emptySet();
    if (CollectionUtils.isNotEmpty(knownProgrammes)) {
      Map<Long, ProgrammeDTO> idsToProgramme = Sets.newHashSet(knownProgrammes).stream()
          .collect(Collectors.toMap((ProgrammeDTO::getId), programme -> programme));

      programmes = associatedProgrammes.stream().map(Long::parseLong).map(idsToProgramme::get)
          .map(programmeDto -> {
            UserProgrammeDTO userProgrammeDto = new UserProgrammeDTO();
            userProgrammeDto.setProgrammeId(programmeDto.getId());
            userProgrammeDto.setProgrammeName(programmeDto.getProgrammeName());
            userProgrammeDto.setProgrammeNumber(programmeDto.getProgrammeNumber());
            return userProgrammeDto;
          }).collect(Collectors.toSet());
    }
    return programmes;
  }

  /**
   * Used for the details page
   *
   * @param heeUserDto The Authorization (profile) user attributes
   * @param authenticationUser The Authentication user attributes
   * @return A composite user from data we hold
   */
  public UserDTO convert(HeeUserDTO heeUserDto, AuthenticationUserDto authenticationUser) {
    UserDTO userDto = new UserDTO();
    mapHeeUserAttributes(userDto, heeUserDto);
    mapAuthUserAttributes(userDto, authenticationUser);

    return userDto;
  }

  private UserDTO mapHeeUserAttributes(UserDTO userDto, HeeUserDTO heeUserDto) {
    if (heeUserDto != null) {
      userDto.setName(heeUserDto.getName());
      userDto.setFirstName(heeUserDto.getFirstName());
      userDto.setLastName(heeUserDto.getLastName());
      userDto.setGmcId(heeUserDto.getGmcId());
      userDto.setPhoneNumber(heeUserDto.getPhoneNumber());
      userDto.setEmailAddress(heeUserDto.getEmailAddress());
      userDto.setLocalOffices(heeUserDto.getDesignatedBodyCodes());
      if (CollectionUtils.isNotEmpty(heeUserDto.getRoles())) {
        Set<String> setOfRoles = heeUserDto.getRoles().stream().map(RoleDTO::getName)
            .collect(Collectors.toSet());
        userDto.setRoles(setOfRoles);
      }

      Set<UserTrustDTO> associatedTrusts = heeUserDto.getAssociatedTrusts();
      if (CollectionUtils.isNotEmpty(associatedTrusts)) {
        Set<String> trustIds = associatedTrusts.stream().map(UserTrustDTO::getTrustId)
            .map(Object::toString)
            .collect(Collectors.toSet());
        userDto.setAssociatedTrusts(trustIds);
      }

      Set<UserProgrammeDTO> associatedProgrammes = heeUserDto.getAssociatedProgrammes();
      if (CollectionUtils.isNotEmpty(associatedProgrammes)) {
        Set<String> programmeIds = associatedProgrammes.stream()
            .map(UserProgrammeDTO::getProgrammeId).map(Object::toString)
            .collect(Collectors.toSet());
        userDto.setAssociatedProgrammes(programmeIds);
      }
    }
    return userDto;
  }

  private UserDTO mapAuthUserAttributes(UserDTO userDto,
      AuthenticationUserDto authenticationUserDto) {
    if (authenticationUserDto != null) {
      userDto.setKcId(authenticationUserDto.getId());
      userDto.setActive(authenticationUserDto.isEnabled());
    }
    return userDto;
  }
}
