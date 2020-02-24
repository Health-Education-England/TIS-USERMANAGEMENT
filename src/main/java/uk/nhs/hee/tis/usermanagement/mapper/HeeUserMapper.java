package uk.nhs.hee.tis.usermanagement.mapper;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.transform.hee.tis.keycloak.User;
import com.transformuk.hee.tis.profile.dto.RoleDTO;
import com.transformuk.hee.tis.profile.service.dto.HeeUserDTO;
import com.transformuk.hee.tis.profile.service.dto.UserTrustDTO;
import com.transformuk.hee.tis.reference.api.dto.DBCDTO;
import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import uk.nhs.hee.tis.usermanagement.DTOs.CreateUserDTO;
import uk.nhs.hee.tis.usermanagement.DTOs.UserDTO;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeDTO;
import com.transformuk.hee.tis.profile.service.dto.UserProgrammeDTO;


import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class HeeUserMapper {

  public List<UserDTO> convertAll(List<HeeUserDTO> heeUserDTOS) {
    return heeUserDTOS.stream().map(this::convert).collect(Collectors.toList());
  }

  /**
   * Used for the list page where only the profile service info is needed
   *
   * @param heeUserDTO
   * @return
   */
  public UserDTO convert(HeeUserDTO heeUserDTO) {
    UserDTO userDTO = new UserDTO();
    return mapHeeUserAttributes(userDTO, heeUserDTO);
  }

  public HeeUserDTO convert(UserDTO userDTO, List<TrustDTO> knownTrusts, List<ProgrammeDTO> knownProgrammes) {
    Preconditions.checkNotNull(userDTO, "stop being stooopid");
    return mapUserAttributes(userDTO.getName(), userDTO.getFirstName(), userDTO.getLastName(), userDTO.getGmcId(),
        userDTO.getPhoneNumber(), userDTO.getEmailAddress(), userDTO.getActive(), userDTO.getLocalOffices(),
        userDTO.getRoles(), userDTO.getEntities(), userDTO.getAssociatedTrusts(),
        userDTO.getAssociatedProgrammes(), knownTrusts, knownProgrammes);
  }

  public HeeUserDTO convert(CreateUserDTO createUserDTO, List<TrustDTO> knownTrusts, List<ProgrammeDTO> knownProgrammes) {
    Preconditions.checkNotNull(createUserDTO, "stop being stooopid");
    HeeUserDTO heeUserDTO = mapUserAttributes(createUserDTO.getName(), createUserDTO.getFirstName(),
        createUserDTO.getLastName(), createUserDTO.getGmcId(), createUserDTO.getPhoneNumber(),
        createUserDTO.getEmailAddress(), createUserDTO.isActive(), createUserDTO.getLocalOffices(),
        createUserDTO.getRoles(), createUserDTO.getEntities(),
        createUserDTO.getAssociatedTrusts(), createUserDTO.getAssociatedProgrammes(), knownTrusts, knownProgrammes);
    heeUserDTO.setPassword(createUserDTO.getPassword());
    heeUserDTO.setTemporaryPassword(createUserDTO.getTempPassword());
    return heeUserDTO;

  }

  private HeeUserDTO mapUserAttributes(String name, String firstName, String lastName, String gmcId, String phoneNumber,
      String emailAddress, boolean active, Set<String> localOffices, Set<String> roles, Set<String> entities,
      Set<String> associatedTrusts, Set<String> associatedProgrammes, List<TrustDTO> knownTrusts, List<ProgrammeDTO> knownProgrammes) {
    HeeUserDTO heeUserDTO = new HeeUserDTO();
    heeUserDTO.setName(name);
    heeUserDTO.setFirstName(firstName);
    heeUserDTO.setLastName(lastName);
    heeUserDTO.setGmcId(gmcId);
    heeUserDTO.setPhoneNumber(phoneNumber);
    heeUserDTO.setEmailAddress(emailAddress);
    heeUserDTO.setActive(active);
    heeUserDTO.setDesignatedBodyCodes(localOffices);
    heeUserDTO.setEntities(entities);

    if (CollectionUtils.isNotEmpty(roles)) {
      Set<RoleDTO> setOfRoles = roles.stream().map(roleName -> {
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setName(roleName);
        return roleDTO;
      }).collect(Collectors.toSet());
      heeUserDTO.setRoles(setOfRoles);
    }
    heeUserDTO.setAssociatedTrusts(mapUserTrust(associatedTrusts, knownTrusts));
    heeUserDTO.setAssociatedProgrammes(mapUserProgramme(associatedProgrammes, knownProgrammes));

    return heeUserDTO;
  }

  private Set<UserTrustDTO> mapUserTrust(Set<String> associatedTrusts, List<TrustDTO> knownTrusts) {
    Set<UserTrustDTO> trusts = Collections.EMPTY_SET;
    if (CollectionUtils.isNotEmpty(associatedTrusts)) {
      Map<Long, TrustDTO> idsToTrust = knownTrusts.stream()
          .collect(Collectors.toMap((TrustDTO::getId), (trust) -> trust));

      trusts = associatedTrusts.stream().map(Long::parseLong).map(idsToTrust::get).map(trustDTO -> {
        UserTrustDTO userTrustDTO = new UserTrustDTO();
        userTrustDTO.setTrustId(trustDTO.getId());
        userTrustDTO.setTrustCode(trustDTO.getCode());
        userTrustDTO.setTrustName(trustDTO.getTrustName());
        return userTrustDTO;
      }).collect(Collectors.toSet());
    }
    return trusts;
  }

  private Set<UserProgrammeDTO> mapUserProgramme(Set<String> associatedProgrammes, List<ProgrammeDTO> knownProgrammes) {
    Set<UserProgrammeDTO> programmes = Collections.EMPTY_SET;
    if (CollectionUtils.isNotEmpty(knownProgrammes)) {
      Map<Long, ProgrammeDTO> idsToProgramme = Sets.newHashSet(knownProgrammes).stream()
          .collect(Collectors.toMap((ProgrammeDTO::getId), (programme) -> programme));

      programmes = associatedProgrammes.stream().map(Long::parseLong).map(idsToProgramme::get).map(programmeDTO -> {
        UserProgrammeDTO userProgrammeDTO = new UserProgrammeDTO();
        userProgrammeDTO.setProgrammeId(programmeDTO.getId());
        userProgrammeDTO.setProgrammeName(programmeDTO.getProgrammeName());
        userProgrammeDTO.setProgrammeNumber(programmeDTO.getProgrammeNumber());
        return userProgrammeDTO;
      }).collect(Collectors.toSet());
    }
    return programmes;
  }

  /**
   * Used for the details page
   *
   * @param heeUserDTO
   * @param keycloakUser
   * @param dbcdtos
   * @return
   */
  public UserDTO convert(HeeUserDTO heeUserDTO, User keycloakUser, Set<DBCDTO> dbcdtos) {
    UserDTO userDTO = new UserDTO();
    mapHeeUserAttributes(userDTO, heeUserDTO);
    mapKeycloakAttributes(userDTO, keycloakUser);
    // mapDBCAttributes(userDTO, heeUserDTO, dbcdtos);

    return userDTO;
  }

  private UserDTO mapHeeUserAttributes(UserDTO userDTO, HeeUserDTO heeUserDTO) {
    if (heeUserDTO != null) {
      userDTO.setName(heeUserDTO.getName());
      userDTO.setFirstName(heeUserDTO.getFirstName());
      userDTO.setLastName(heeUserDTO.getLastName());
      userDTO.setGmcId(heeUserDTO.getGmcId());
      userDTO.setPhoneNumber(heeUserDTO.getPhoneNumber());
      userDTO.setEmailAddress(heeUserDTO.getEmailAddress());
      userDTO.setLocalOffices(heeUserDTO.getDesignatedBodyCodes());
      userDTO.setEntities(heeUserDTO.getEntities());
      if (CollectionUtils.isNotEmpty(heeUserDTO.getRoles())) {
        Set<String> setOfRoles = heeUserDTO.getRoles().stream().map(RoleDTO::getName).collect(Collectors.toSet());
        userDTO.setRoles(setOfRoles);
      }

      Set<UserTrustDTO> associatedTrusts = heeUserDTO.getAssociatedTrusts();
      if (CollectionUtils.isNotEmpty(associatedTrusts)) {
        Set<String> trustIds = associatedTrusts.stream().map(UserTrustDTO::getTrustId).map(Object::toString)
            .collect(Collectors.toSet());
        userDTO.setAssociatedTrusts(trustIds);
      }

      Set<UserProgrammeDTO> associatedProgrammes = heeUserDTO.getAssociatedProgrammes();
      if(CollectionUtils.isNotEmpty(associatedProgrammes)) {
        Set<String> programmeIds = associatedProgrammes.stream().map(UserProgrammeDTO::getProgrammeId).map(Object::toString)
            .collect(Collectors.toSet());
        userDTO.setAssociatedProgrammes(programmeIds);
      }
    }
    return userDTO;
  }

  private UserDTO mapKeycloakAttributes(UserDTO userDTO, User keycloakUser) {
    if (keycloakUser != null) {
      userDTO.setKcId(keycloakUser.getId());
      userDTO.setActive(keycloakUser.getEnabled());
    }
    return userDTO;
  }

  private UserDTO mapDBCAttributes(UserDTO userDTO, HeeUserDTO heeUserDTO, Set<DBCDTO> dbcdtos) {
    Map<String, String> dbcToLocalOffice = dbcdtos.stream().collect(Collectors.toMap(DBCDTO::getDbc, DBCDTO::getName));
    Set<String> setOfLOs = heeUserDTO.getDesignatedBodyCodes().stream().map(dbc -> dbcToLocalOffice.get(dbc))
        .collect(Collectors.toSet());
    userDTO.setLocalOffices(setOfLOs);

    return userDTO;
  }
}
