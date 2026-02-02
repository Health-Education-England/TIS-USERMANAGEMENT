package uk.nhs.hee.tis.usermanagement.DTOs;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Data;

/**
 * Data Transfer Object for User details.
 */
@Data
public class UserDTO {

  private String authId;
  private String name;
  private String firstName;
  private String lastName;
  private String gmcId;
  private String phoneNumber;
  private String emailAddress;
  private boolean active;
  private Set<String> roles = new HashSet<>();
  private Set<String> localOffices = new HashSet<>();
  private Set<String> associatedTrusts = new HashSet<>();
  private Set<String> associatedProgrammes = new HashSet<>();
  private List<String> userMfaSettingList;
  private String preferredMfaSetting;
}
