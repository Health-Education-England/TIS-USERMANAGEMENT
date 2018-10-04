package uk.nhs.hee.tis.usermanagement.mapper;

import com.google.common.base.Preconditions;
import com.transform.hee.tis.keycloak.User;
import com.transformuk.hee.tis.profile.dto.RoleDTO;
import com.transformuk.hee.tis.profile.service.dto.HeeUserDTO;
import com.transformuk.hee.tis.reference.api.dto.DBCDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import uk.nhs.hee.tis.usermanagement.DTOs.UserDTO;

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

  public HeeUserDTO convert(UserDTO userDTO) {
    Preconditions.checkNotNull(userDTO, "stop being stooopid");
    return mapUserAttributes(userDTO);
  }

  private HeeUserDTO mapUserAttributes(UserDTO userDTO) {
    HeeUserDTO heeUserDTO = new HeeUserDTO();
    heeUserDTO.setName(userDTO.getName());
    heeUserDTO.setFirstName(userDTO.getFirstName());
    heeUserDTO.setLastName(userDTO.getLastName());
    heeUserDTO.setGmcId(userDTO.getGmcId());
    heeUserDTO.setPhoneNumber(userDTO.getPhoneNumber());
    heeUserDTO.setEmailAddress(userDTO.getEmailAddress());

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

    heeUserDTO.setAssociatedTrusts(userDTO.getAssociatedTrusts());

    return heeUserDTO;
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
    mapDBCAttributes(userDTO, heeUserDTO, dbcdtos);

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

      if (CollectionUtils.isNotEmpty(heeUserDTO.getRoles())) {
        Set<String> setOfRoles = heeUserDTO.getRoles().stream().map(role -> role.getName()).collect(Collectors.toSet());
        userDTO.setRoles(setOfRoles);
      }

      userDTO.setAssociatedTrusts(heeUserDTO.getAssociatedTrusts());
    }
    return userDTO;
  }

  private UserDTO mapKeycloakAttributes(UserDTO userDTO, User keycloakUser) {
    if (keycloakUser != null) {
      userDTO.setActive(keycloakUser.getEnabled());
      userDTO.setTemporaryPassword(keycloakUser.getTempPassword());
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
