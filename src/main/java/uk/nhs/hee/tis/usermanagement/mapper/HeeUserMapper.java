package uk.nhs.hee.tis.usermanagement.mapper;

import com.google.common.base.Preconditions;
import com.transform.hee.tis.keycloak.User;
import com.transformuk.hee.tis.profile.dto.RoleDTO;
import com.transformuk.hee.tis.profile.service.dto.HeeUserDTO;
import com.transformuk.hee.tis.profile.service.dto.UserTrustDTO;
import com.transformuk.hee.tis.reference.api.dto.DBCDTO;
import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import uk.nhs.hee.tis.usermanagement.DTOs.UserDTO;

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

  public HeeUserDTO convert(UserDTO userDTO, List<TrustDTO> knownTrusts) {
    Preconditions.checkNotNull(userDTO, "stop being stooopid");
    return mapUserAttributes(userDTO, knownTrusts);
  }

  private HeeUserDTO mapUserAttributes(UserDTO userDTO, List<TrustDTO> knownTrusts) {
    HeeUserDTO heeUserDTO = new HeeUserDTO();
    heeUserDTO.setName(userDTO.getName());
    heeUserDTO.setFirstName(userDTO.getFirstName());
    heeUserDTO.setLastName(userDTO.getLastName());
    heeUserDTO.setGmcId(userDTO.getGmcId());
    heeUserDTO.setPhoneNumber(userDTO.getPhoneNumber());
    heeUserDTO.setEmailAddress(userDTO.getEmailAddress());
    heeUserDTO.setDesignatedBodyCodes(userDTO.getLocalOffices());

    if (CollectionUtils.isNotEmpty(userDTO.getRoles())) {
      Set<RoleDTO> setOfRoles = userDTO.getRoles()
          .stream()
          .map(roleName -> {
            RoleDTO roleDTO = new RoleDTO();
            roleDTO.setName(roleName);
            return roleDTO;
          }).collect(Collectors.toSet());
      heeUserDTO.setRoles(setOfRoles);
    }

    Set<String> associatedTrusts = userDTO.getAssociatedTrusts();

    heeUserDTO.setAssociatedTrusts(mapUserTrust(associatedTrusts, knownTrusts));

    return heeUserDTO;
  }

  private Set<UserTrustDTO> mapUserTrust(Set<String> associatedTrusts, List<TrustDTO> knownTrusts) {
    Set<UserTrustDTO> trusts = Collections.EMPTY_SET;
    if (CollectionUtils.isNotEmpty(associatedTrusts)) {
      Map<Long, TrustDTO> idsToTrust = knownTrusts.stream().collect(Collectors.toMap((TrustDTO::getId), (trust) -> trust));

      trusts = associatedTrusts.stream()
          .map(Long::parseLong)
          .map(idsToTrust::get)
          .map(trustDTO -> {
            UserTrustDTO userTrustDTO = new UserTrustDTO();
            userTrustDTO.setTrustId(trustDTO.getId());
            userTrustDTO.setTrustCode(trustDTO.getCode());
            userTrustDTO.setTrustName(trustDTO.getTrustName());
            return userTrustDTO;
          })
          .collect(Collectors.toSet());
    }
    return trusts;
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
//    mapDBCAttributes(userDTO, heeUserDTO, dbcdtos);

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
      if (CollectionUtils.isNotEmpty(heeUserDTO.getRoles())) {
        Set<String> setOfRoles = heeUserDTO.getRoles().stream().map(RoleDTO::getName).collect(Collectors.toSet());
        userDTO.setRoles(setOfRoles);
      }

      Set<UserTrustDTO> associatedTrusts = heeUserDTO.getAssociatedTrusts();
      if (CollectionUtils.isNotEmpty(associatedTrusts)) {
        Set<String> trustIds = associatedTrusts.stream()
            .map(UserTrustDTO::getTrustId)
            .map(Object::toString).collect(Collectors.toSet());
        userDTO.setAssociatedTrusts(trustIds);
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
    Set<String> setOfLOs = heeUserDTO.getDesignatedBodyCodes().stream().map(dbc -> dbcToLocalOffice.get(dbc)).collect(Collectors.toSet());
    userDTO.setLocalOffices(setOfLOs);

    return userDTO;
  }
}
