package contacts.dto;

import lombok.Data;

/**
 * Data Transfer Object for user creation and update operations.
 */
@Data
public class UserDTO {
    private String username;
    private String password;
    private String oldPassword;
    private String role;
}
