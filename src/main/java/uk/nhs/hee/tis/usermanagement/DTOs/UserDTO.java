package uk.nhs.hee.tis.usermanagement.DTOs;

import java.util.HashSet;
import java.util.Set;

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

  public String getAuthId() {
    return authId;
  }

  public void setAuthId(String authId) {
    this.authId = authId;
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

  public boolean getActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
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

  public Set<String> getAssociatedProgrammes() {
    return associatedProgrammes;
  }

  public void setAssociatedProgrammes(Set<String> associatedProgrammes) {
    this.associatedProgrammes = associatedProgrammes;
  }
}
