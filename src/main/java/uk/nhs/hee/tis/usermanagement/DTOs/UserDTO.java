package uk.nhs.hee.tis.usermanagement.DTOs;

import java.util.HashSet;
import java.util.Set;

public class UserDTO {
  private String kcId;
  private String name;
  private String firstName;
  private String lastName;
  private String gmcId;
  private String phoneNumber;
  private String emailAddress;
  private Boolean active;
  private String password;
  private Boolean isTemporaryPassword;
  private Set<String> roles = new HashSet<>();
  private Set<String> localOffices = new HashSet<>();
  private Set<String> associatedTrusts = new HashSet<>();

  public String getKcId() {
    return kcId;
  }

  public void setKcId(String kcId) {
    this.kcId = kcId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getGmcId() {
    return gmcId;
  }

  public void setGmcId(String gmcId) {
    this.gmcId = gmcId;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public String getEmailAddress() {
    return emailAddress;
  }

  public void setEmailAddress(String emailAddress) {
    this.emailAddress = emailAddress;
  }

  public Boolean getActive() {
    return active;
  }

  public void setActive(Boolean active) {
    this.active = active;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public Boolean getTemporaryPassword() {
    return isTemporaryPassword;
  }

  public void setTemporaryPassword(Boolean temporaryPassword) {
    isTemporaryPassword = temporaryPassword;
  }

  public Set<String> getRoles() {
    return roles;
  }

  public void setRoles(Set<String> roles) {
    this.roles = roles;
  }

  public Set<String> getLocalOffices() {
    return localOffices;
  }

  public void setLocalOffices(Set<String> localOffices) {
    this.localOffices = localOffices;
  }

  public Set<String> getAssociatedTrusts() {
    return associatedTrusts;
  }

  public void setAssociatedTrusts(Set<String> associatedTrusts) {
    this.associatedTrusts = associatedTrusts;
  }
}
