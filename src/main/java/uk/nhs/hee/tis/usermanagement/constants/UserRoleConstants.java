package uk.nhs.hee.tis.usermanagement.constants;

import java.util.Set;

/**
 * Class of constants for user roles.
 */
public final class UserRoleConstants {

  private UserRoleConstants() {
  }

  public static final Set<String> ROLES_REQUIRING_TRUST = Set.of(
      "HEE Trust Observer",
      "HEE Trust Admin"
  );

  public static final Set<String> ROLES_REQUIRING_PROGRAMME = Set.of(
      "HEE Programme Observer",
      "HEE Programme Admin"
  );
}
