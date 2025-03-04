package com.example.realestate.dto.profile;

import lombok.Data;

/**
 * Fields sent by the frontend when the user updates
 * their username/password on the profile page.
 */
@Data
public class ProfileRequestDto {
    private String username;            // Optional - new username
    private String oldPassword;         // Required if newPassword is set
    private String newPassword;         // Optional 
    private String confirmNewPassword;  // Optional
}
