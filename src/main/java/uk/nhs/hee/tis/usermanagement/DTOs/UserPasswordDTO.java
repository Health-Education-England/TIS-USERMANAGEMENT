package uk.nhs.hee.tis.usermanagement.DTOs;

import java.io.Serializable;

public class UserPasswordDTO implements Serializable {

    private String kcId;
    private String password;
    private String confirmPassword;
    private boolean tempPassword;

    public String getKcId() {
        return kcId;
    }

    public void setKcId(String kcId) {
        this.kcId = kcId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

  public boolean isTempPassword() {
    return tempPassword;
  }

  public void setTempPassword(boolean tempPassword) {
    this.tempPassword = tempPassword;
  }
}
